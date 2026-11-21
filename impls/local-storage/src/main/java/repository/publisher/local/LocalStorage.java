package repository.publisher.local;

import repository.common.RepositoryException;
import repository.common.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LocalStorage implements Storage {

    private Path root;

    @Override
    public void createRepositoryAt(String location) {
        File dir = new File(location);

        if (dir.exists()) {
            // ako folder vec postoji mora da bude prazan, da ne bi pregazili neki repo
            String[] content = dir.list();
            if (content == null) {
                throw new RepositoryException("Nije folder: " + location);
            }
            if (content.length > 0) {
                throw new RepositoryException("Folder nije prazan: " + location);
            }
        } else if (!dir.mkdirs()) {
            throw new RepositoryException("Ne moze da se napravi folder: " + location);
        }

        setRoot(dir.toPath());
    }

    @Override
    public void openRepositoryAt(String location) {
        File dir = new File(location);
        if (!dir.isDirectory())
            throw new RepositoryException("Repozitorijum ne postoji: " + location);

        setRoot(dir.toPath());
    }

    private void setRoot(Path dir) {
        root = dir.toAbsolutePath().normalize();
    }

    @Override
    public void createDirectories(String path) {
        try {
            Files.createDirectories(resolve(path));
        } catch (IOException e) {
            throw new RepositoryException("Greska pri pravljenju foldera: " + path, e);
        }
    }

    @Override
    public void writeNew(String relativePath, InputStream content) {
        Path target = resolve(relativePath);

        // ne smemo da prepisemo postojeci fajl
        if (Files.exists(target)) {
            throw new RepositoryException("Fajl vec postoji: " + relativePath);
        }

        try {
            Files.copy(content, target);
        } catch (IOException e) {
            throw new RepositoryException("Greska pri upisu: " + relativePath, e);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        return Files.exists(resolve(relativePath));
    }

    @Override
    public List<String> listDirectories(String relativePath) {
        return list(relativePath, Files::isDirectory);
    }

    @Override
    public List<String> listFiles(String relativePath) {
        return list(relativePath, Files::isRegularFile);
    }

    @Override
    public InputStream read(String relativePath) {
        Path file = resolve(relativePath);
        if (!Files.isRegularFile(file)) {
            throw new RepositoryException("File not found: " + relativePath);
        }
        try {
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RepositoryException("Error reading " + relativePath, e);
        }
    }

    private List<String> list(String relativePath, Predicate<Path> filter) {
        Path dir = resolve(relativePath);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(filter).map(p -> p.getFileName().toString()).toList();
        } catch (IOException e) {
            throw new RepositoryException("Error listing " + relativePath, e);
        }
    }

    // ne dozvoljava izlazak iz korena repozitorijuma (npr. "../../etc")
    protected Path resolve(String relativePath) {
        checkOpened();

        Path relative = Path.of(relativePath);
        if (relative.isAbsolute())
            throw new RepositoryException("Ocekivana relativna putanja: " + relativePath);

        Path p = root.resolve(relativePath).normalize();
        if (!p.startsWith(root))
            throw new RepositoryException("Path outside repository: " + relativePath);

        return p;
    }

    private void checkOpened() {
        if (root == null) {
            throw new RepositoryException("Repozitorijum nije otvoren, prvo pozovi createRepositoryAt ili openRepositoryAt");
        }
    }
}
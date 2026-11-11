package repository.publisher.local;

import repository.common.RepositoryException;
import repository.common.RepositoryWriter;
import resolver.local.LocalRepositoryReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// lokalni writer, repo je obican folder na disku
// za citanje koristim LocalRepositoryReader iz resolvera da ne pisem isto dva puta
public class LocalRepositoryWriter implements RepositoryWriter {

    private Path root;
    private LocalRepositoryReader reader;

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
        if (!dir.isDirectory()) {
            throw new RepositoryException("Repozitorijum ne postoji: " + location);
        }
        setRoot(dir.toPath());
    }

    private void setRoot(Path dir) {
        root = dir.toAbsolutePath().normalize();
        reader = new LocalRepositoryReader(root);
    }

    @Override
    public void createDirectories(String path) {
        try {
            Files.createDirectories(toFullPath(path));
        } catch (IOException e) {
            throw new RepositoryException("Greska pri pravljenju foldera: " + path, e);
        }
    }

    @Override
    public void writeNew(String relativePath, InputStream content) {
        Path target = toFullPath(relativePath);

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

    // citanje samo prosledjujem readeru

    @Override
    public boolean exists(String relativePath) {
        checkOpened();
        return reader.exists(relativePath);
    }

    @Override
    public List<String> listDirectories(String relativePath) {
        checkOpened();
        return reader.listDirectories(relativePath);
    }

    @Override
    public List<String> listFiles(String relativePath) {
        checkOpened();
        return reader.listFiles(relativePath);
    }

    @Override
    public InputStream read(String relativePath) {
        checkOpened();
        return reader.read(relativePath);
    }

    private void checkOpened() {
        if (root == null) {
            throw new RepositoryException("Repozitorijum nije otvoren, prvo pozovi createRepositoryAt ili openRepositoryAt");
        }
    }

    // pretvara relativnu putanju u punu i pazi da ne izadje iz repoa (npr ../../nesto)
    private Path toFullPath(String relativePath) {
        checkOpened();
        Path full = root.resolve(relativePath).normalize();
        if (!full.startsWith(root)) {
            throw new RepositoryException("Putanja izlazi iz repozitorijuma: " + relativePath);
        }
        return full;
    }
}
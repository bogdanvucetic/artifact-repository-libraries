package resolver.local;

import repository.common.RepositoryException;
import repository.common.RepositoryReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Lokalni "drajver": relativne putanje repozitorijuma mapira na folder na disku. */
public class LocalRepositoryReader implements RepositoryReader {

    protected final Path root;

    public LocalRepositoryReader(Path root) {
        if (!Files.isDirectory(root)) {
            throw new RepositoryException("Repository does not exist: " + root);
        }
        this.root = root.toAbsolutePath().normalize();
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
        Path p = root.resolve(relativePath).normalize();
        if (!p.startsWith(root)) {
            throw new RepositoryException("Path outside repository: " + relativePath);
        }
        return p;
    }
}
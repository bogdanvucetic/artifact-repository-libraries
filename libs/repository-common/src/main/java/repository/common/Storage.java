package repository.common;

import java.io.InputStream;
import java.util.List;

public interface Storage {
    boolean exists(String relativePath);

    List<String> listDirectories(String relativePath);

    List<String> listFiles(String relativePath);

    InputStream read(String relativePath);

    void createRepositoryAt(String location);

    void openRepositoryAt(String location);

    void createDirectories(String path);

    void createFile(String relativePath, InputStream content);
}

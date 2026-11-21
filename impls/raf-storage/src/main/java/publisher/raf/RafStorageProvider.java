package publisher.raf;

import repository.common.RepositoryException;
import repository.common.Storage;
import repository.common.StorageProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class RafStorageProvider implements StorageProvider {
    @Override
    public String name() {
        return "raf";
    }

    @Override
    public Storage create(Map<String, String> options) {
        String configPath = System.getenv("RAF_CONFIG");

        if (configPath == null || configPath.isBlank())
            configPath = ".config/raf";

        Properties config = new Properties();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(configPath))) {
            config.load(reader);
        } catch (IOException e) {
            throw new RepositoryException("Neuspesno citanje RAF konfiguracije: " + configPath, e);
        }

        String host = config.getProperty("host");
        String username = config.getProperty("username");
        String password = config.getProperty("password");

        if (host == null || host.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank())
            throw new RepositoryException("Nedostaju podaci u RAF konfiguraciji");

        return new RafStorage(host, username, password);
    }
}

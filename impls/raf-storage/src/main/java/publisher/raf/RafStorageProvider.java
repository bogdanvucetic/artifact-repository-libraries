package publisher.raf;

import repository.common.Storage;
import repository.common.StorageProvider;

import java.util.Map;

public class RafStorageProvider implements StorageProvider {
    @Override
    public String name() {
        return "raf";
    }

    @Override
    public Storage create(Map<String, String> options) {
        return new RafStorage("sftp.example.invalid", "REDACTED_USER", "REDACTED_PASSWORD");
    }
}

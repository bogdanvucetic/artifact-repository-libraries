package repository.publisher.drive;

import repository.common.Storage;
import repository.common.StorageProvider;

import java.util.Map;

public class DriveStorageProvider implements StorageProvider {

    @Override
    public String name() {
        return "gdrive";
    }

    @Override
    public Storage create(Map<String, String> options) {
        return new DriveStorage();
    }
}
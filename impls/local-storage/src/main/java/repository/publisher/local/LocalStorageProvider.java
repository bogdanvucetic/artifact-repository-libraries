package repository.publisher.local;

import repository.common.Storage;
import repository.common.StorageProvider;

import java.util.Map;

public class LocalStorageProvider implements StorageProvider {

    @Override
    public String name() {
        return "local";
    }

    @Override
    public Storage create(Map<String, String> options) {
        return new LocalStorage();
    }
}
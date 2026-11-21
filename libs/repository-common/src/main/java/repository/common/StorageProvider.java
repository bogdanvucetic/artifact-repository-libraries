package repository.common;

import java.util.Map;

public interface StorageProvider {
    String name();
    Storage create(Map<String, String> options);
}

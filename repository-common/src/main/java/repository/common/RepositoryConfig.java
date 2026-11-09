package repository.common;

import java.util.Map;
import java.util.Set;

public record RepositoryConfig(
        Set<String> allowedExtensions,
        Map<String, Boolean> scopes
) {
}

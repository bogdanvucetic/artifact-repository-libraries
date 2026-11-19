package repository.common;

import java.util.Map;
import java.util.Set;

// todo:
//  - provera da nijedna ekstenzija ne pocinje sa .
//  - provera da postoji makar jedna ekstenzija
//  - provera da postoji makar jedan scope
public record RepositoryConfig(
        Set<String> allowedExtensions,
        Map<String, Boolean> scopes
) {
    public RepositoryConfig {
        allowedExtensions = Set.copyOf(allowedExtensions);
        scopes = Map.copyOf(scopes);
    }
}

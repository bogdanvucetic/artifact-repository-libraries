package org.example.repository.common;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record RepositoryConfig(
        Set<String> allowedExtensions,
        Map<String, Boolean> scopes
) {
    public RepositoryConfig {
        allowedExtensions = Set.copyOf(allowedExtensions);
        scopes = Map.copyOf(scopes);

        // todo: možda dodatne provere, tipa da ekstenzije nisu ".", da postoji makar jedna ekstenzija i sl.?
        //  ili pak u Publisher?
    }
}

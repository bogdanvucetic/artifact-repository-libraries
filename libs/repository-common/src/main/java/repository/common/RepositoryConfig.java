package repository.common;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record RepositoryConfig(
        Set<String> allowedExtensions,
        Map<String, Boolean> scopes
) {
    public RepositoryConfig {
        Objects.requireNonNull(allowedExtensions, "Ekstenzije ne smeju biti null");
        Objects.requireNonNull(scopes, "Scope-ovi ne smeju biti null");

        if (allowedExtensions.isEmpty()) throw new IllegalArgumentException(
                "Repozitorijum mora prihvatiti makar jednu ekstenziju");
        if (scopes.isEmpty()) throw new IllegalArgumentException(
                "Repozitorijum mora prihvatiti makar jedan scope");

        for (String extension : allowedExtensions) {
            Objects.requireNonNull(extension, "Nijedna ekstenzija ne sme biti null");
            if (extension.isBlank())
                throw new IllegalArgumentException("Ekstenzija ne sme biti prazna");

            if (extension.startsWith(".")) {
                throw new IllegalArgumentException("Nevalidna ekstenzija: "
                        + extension + ". Ekstenzije ne smeju da pocnu tackom");
            }
        }

        for (Map.Entry<String, Boolean> scope : scopes.entrySet()) {
            Objects.requireNonNull(scope.getKey(), "Nijedan scope ne sme biti null");
            if (scope.getKey().isBlank())
                throw new IllegalArgumentException("Scope ne sme biti prazan");

            Objects.requireNonNull(scope.getValue(), "Nijedan scope ne sme biti null");
        }

        allowedExtensions = Set.copyOf(allowedExtensions);
        scopes = Map.copyOf(scopes);
    }
}

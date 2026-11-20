package repository.common;

import java.util.Objects;

public record Dependency(
        ComponentId id,
        String scope,
        boolean optional
) {
    public Dependency {
        Objects.requireNonNull(id, "ID ne sme biti null");
        Objects.requireNonNull(scope, "Scope ne sme biti null");

        if (scope.isBlank())
            throw new IllegalArgumentException("Scope ne sme biti prazan");
    }
}

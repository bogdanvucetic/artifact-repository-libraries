package org.example.repository.common;

import java.util.Objects;

public record Dependency(
        ComponentId id,
        String scope,
        boolean optional
) {
    public Dependency {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(scope, "Scope cannot be null");
    }
}

package org.example.repository.common;

public record Dependency(
        ComponentId id,
        String scope,
        boolean optional
) {
}

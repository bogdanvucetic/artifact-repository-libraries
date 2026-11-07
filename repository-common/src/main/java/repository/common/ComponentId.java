package org.example.repository.common;

public record ComponentId(
        String organization,
        String name,
        String version
) {
}

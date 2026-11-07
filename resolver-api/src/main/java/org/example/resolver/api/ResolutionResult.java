package org.example.resolver.api;

import org.example.repository.common.ComponentId;

import java.nio.file.Path;
import java.util.Map;

public record ResolutionResult(
        Map<ComponentId, Path> downloaded   // odakle
) {
}
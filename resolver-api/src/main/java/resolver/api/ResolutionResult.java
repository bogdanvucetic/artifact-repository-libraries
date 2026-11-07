package resolver.api;

import repository.common.ComponentId;

import java.nio.file.Path;
import java.util.Map;

public record ResolutionResult(
        Map<ComponentId, Path> downloaded   // odakle
) {
}
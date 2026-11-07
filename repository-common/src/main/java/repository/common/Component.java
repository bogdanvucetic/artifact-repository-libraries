package repository.common;

import java.util.List;

public record Component(
        ComponentId id,
        List<Dependency> dependencies
) {
}

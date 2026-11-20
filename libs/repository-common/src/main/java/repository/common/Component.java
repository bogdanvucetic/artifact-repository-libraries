package repository.common;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Component(
        ComponentId id,
        List<Dependency> dependencies
) {
    public Component {
        Objects.requireNonNull(id, "ID cannot be null");

        Set<ComponentId> ids = new HashSet<>();

        for (Dependency d : dependencies) {
            if (ids.contains(d.id())) {
                throw new IllegalArgumentException("Duplicate dependency:" + d.id());
            }
            ids.add(d.id());
        }

        dependencies = List.copyOf(dependencies);
    }
}

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
        Objects.requireNonNull(id, "ID ne sme biti null");
        Objects.requireNonNull(dependencies, "Lista zavisnosti ne sme biti null");

        Set<ComponentId> seen = new HashSet<>();

        for (Dependency d : dependencies) {
            Objects.requireNonNull(d, "Zavisnost ne sme biti null");
            if (seen.contains(d.id()))
                throw new IllegalArgumentException("Dupla zavisnost:" + d.id());

            seen.add(d.id());
        }

        dependencies = List.copyOf(dependencies);
    }
}

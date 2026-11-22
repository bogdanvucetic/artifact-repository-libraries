package repository.publisher;

import repository.common.Component;
import repository.common.ComponentId;
import repository.common.Dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ComponentBuilder {
    private ComponentId id;
    private final List<Dependency> dependencies = new ArrayList<>();

    public ComponentBuilder id(String organization, String name, String version) {
        id = new ComponentId(organization, name, version);
        return this;
    }

    public ComponentBuilder id(ComponentId id) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        return this;
    }

    public ComponentBuilder addDependency(Dependency dependency) {
        Objects.requireNonNull(dependency, "Dependency cannot be null");

        for (Dependency d : dependencies) {
            if (d.id().equals(dependency.id())) {
                throw new IllegalArgumentException("Duplicate dependency:" + dependency.id());
            }
        }

        dependencies.add(dependency);
        return this;
    }

    public ComponentBuilder removeDependency(String organization, String name, String version) {
        return removeDependency(new ComponentId(organization, name, version));
    }

    public ComponentBuilder removeDependency(ComponentId id) {
        Objects.requireNonNull(id, "ID cannot be null");

        dependencies.removeIf(d -> d.id().equals(id));

        return this;
    }

    public ComponentBuilder addDependencies(Collection<Dependency> dependencies) {
        for (Dependency d : dependencies) {
            addDependency(d);
        }
        return this;
    }

    public Component build() {
        return new Component(id, dependencies);
    }
}

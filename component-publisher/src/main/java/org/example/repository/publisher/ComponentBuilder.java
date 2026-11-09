package org.example.repository.publisher;

import org.example.repository.common.Component;
import org.example.repository.common.ComponentId;
import org.example.repository.common.Dependency;

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

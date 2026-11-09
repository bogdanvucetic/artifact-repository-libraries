
package org.example.repository.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComponentTest {

    private final ComponentId id =
            new ComponentId("org.example", "app", "1.0.0");

    private final ComponentId dependencyId =
            new ComponentId("org.example", "utils", "1.0.0");

    @Test
    void rejectsNullId() {
        assertThrows(NullPointerException.class,
                () -> new Component(null, List.of()));
    }

    @Test
    void rejectsNullDependencies() {
        assertThrows(NullPointerException.class,
                () -> new Component(id, null));
    }

    @Test
    void rejectsNullDependencyElement() {
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(null);

        assertThrows(NullPointerException.class,
                () -> new Component(id, dependencies));
    }

    @Test
    void rejectsDuplicateDependencyIds() {
        Dependency first =
                new Dependency(dependencyId, "compile", false);

        Dependency second =
                new Dependency(dependencyId, "runtime", true);

        assertThrows(IllegalArgumentException.class,
                () -> new Component(id, List.of(first, second)));
    }

    @Test
    void copiesDependenciesDefensively() {
        List<Dependency> dependencies = new ArrayList<>();
        Component component = new Component(id, dependencies);

        dependencies.add(
                new Dependency(dependencyId, "compile", false)
        );

        assertTrue(component.dependencies().isEmpty());
    }

    @Test
    void dependenciesCannotBeModified() {
        Component component = new Component(id, List.of());

        assertThrows(UnsupportedOperationException.class,
                () -> component.dependencies().add(
                        new Dependency(dependencyId, "compile", false)
                ));
    }
}

package org.example.repository.publisher;

import org.example.repository.common.Component;
import org.example.repository.common.ComponentId;
import org.example.repository.common.Dependency;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComponentBuilderTest {

    private final ComponentId id =
            new ComponentId("org.example", "app", "1.0.0");

    private final ComponentId dependencyId =
            new ComponentId("org.example", "utils", "1.0.0");

    @Test
    void buildsComponent() {
        Component component = new ComponentBuilder()
                .id(id)
                .build();

        assertEquals(id, component.id());
        assertTrue(component.dependencies().isEmpty());
    }

    @Test
    void buildsComponentWithStringId() {
        Component component = new ComponentBuilder()
                .id("org.example", "app", "1.0.0")
                .build();

        assertEquals(id, component.id());
    }

    @Test
    void rejectsMissingId() {
        assertThrows(NullPointerException.class,
                () -> new ComponentBuilder().build());
    }

    @Test
    void rejectsNullId() {
        ComponentBuilder builder = new ComponentBuilder();

        assertThrows(NullPointerException.class,
                () -> builder.id((ComponentId) null));
    }

    @Test
    void rejectsNullDependency() {
        ComponentBuilder builder = new ComponentBuilder();

        assertThrows(NullPointerException.class,
                () -> builder.addDependency(null));
    }

    @Test
    void rejectsDuplicateDependencies() {
        Dependency first =
                new Dependency(dependencyId, "compile", false);

        Dependency second =
                new Dependency(dependencyId, "runtime", true);

        ComponentBuilder builder = new ComponentBuilder()
                .id(id)
                .addDependency(first);

        assertThrows(IllegalArgumentException.class,
                () -> builder.addDependency(second));
    }

    @Test
    void rejectsDuplicatesWithinCollection() {
        Dependency first =
                new Dependency(dependencyId, "compile", false);

        Dependency second =
                new Dependency(dependencyId, "runtime", true);

        ComponentBuilder builder = new ComponentBuilder()
                .id(id);

        assertThrows(IllegalArgumentException.class,
                () -> builder.addDependencies(List.of(first, second)));
    }

    @Test
    void addsMultipleDependencies() {
        Dependency first = new Dependency(
                new ComponentId("org.example", "utils", "1.0.0"),
                "compile",
                false
        );

        Dependency second = new Dependency(
                new ComponentId("org.example", "logger", "2.0.0"),
                "runtime",
                true
        );

        Component component = new ComponentBuilder()
                .id(id)
                .addDependencies(List.of(first, second))
                .build();

        assertEquals(2, component.dependencies().size());
        assertTrue(component.dependencies().containsAll(
                List.of(first, second)
        ));
    }

    @Test
    void builtComponentIsIndependentOfBuilder() {
        ComponentBuilder builder = new ComponentBuilder().id(id);

        Component component = builder.build();

        builder.addDependency(
                new Dependency(dependencyId, "compile", false)
        );

        assertTrue(component.dependencies().isEmpty());
    }
}

package repository.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyTest {

    private final ComponentId id =
            new ComponentId("org.example", "utils", "1.0.0");

    @Test
    void createsValidDependency() {
        Dependency dependency =
                new Dependency(id, "compile", false);

        assertEquals(id, dependency.id());
        assertEquals("compile", dependency.scope());
        assertFalse(dependency.optional());
    }

    @Test
    void createsOptionalDependency() {
        Dependency dependency =
                new Dependency(id, "runtime", true);

        assertTrue(dependency.optional());
    }

    @Test
    void rejectsNullId() {
        assertThrows(NullPointerException.class,
                () -> new Dependency(null, "compile", false));
    }

    @Test
    void rejectsNullScope() {
        assertThrows(NullPointerException.class,
                () -> new Dependency(id, null, false));
    }

    @Test
    void equalDependenciesHaveEqualHashCodes() {
        Dependency first =
                new Dependency(id, "compile", false);

        Dependency second =
                new Dependency(
                        new ComponentId("org.example", "utils", "1.0.0"),
                        "compile",
                        false
                );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void differentScopesProduceUnequalDependencies() {
        Dependency first =
                new Dependency(id, "compile", false);

        Dependency second =
                new Dependency(id, "runtime", false);

        assertNotEquals(first, second);
    }

    @Test
    void differentOptionalityProducesUnequalDependencies() {
        Dependency first =
                new Dependency(id, "compile", false);

        Dependency second =
                new Dependency(id, "compile", true);

        assertNotEquals(first, second);
    }

    @Test
    void differentIdsProduceUnequalDependencies() {
        Dependency first =
                new Dependency(id, "compile", false);

        Dependency second =
                new Dependency(
                        new ComponentId("org.example", "logger", "1.0.0"),
                        "compile",
                        false
                );

        assertNotEquals(first, second);
    }

    @Test
    void toStringContainsDependencyFields() {
        Dependency dependency =
                new Dependency(id, "compile", true);

        String result = dependency.toString();

        assertTrue(result.contains("compile"));
        assertTrue(result.contains("optional=true"));
        assertTrue(result.contains("Dependency"));
    }
}

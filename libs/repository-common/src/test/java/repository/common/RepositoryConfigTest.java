package repository.common;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryConfigTest {

    @Test
    void rejectsNullExtensions() {
        assertThrows(NullPointerException.class,
                () -> new RepositoryConfig(null, Map.of()));
    }

    @Test
    void rejectsNullScopes() {
        assertThrows(NullPointerException.class,
                () -> new RepositoryConfig(Set.of(), null));
    }

    @Test
    void copiesExtensionsDefensively() {
        Set<String> extensions = new HashSet<>();
        extensions.add("jar");

        RepositoryConfig config =
                new RepositoryConfig(extensions, Map.of());

        extensions.add("zip");

        assertFalse(config.allowedExtensions().contains("zip"));
    }

    @Test
    void copiesScopesDefensively() {
        Map<String, Boolean> scopes = new HashMap<>();
        scopes.put("compile", true);

        RepositoryConfig config =
                new RepositoryConfig(Set.of("jar"), scopes);

        scopes.put("runtime", false);

        assertFalse(config.scopes().containsKey("runtime"));
    }

    @Test
    void extensionsCannotBeModified() {
        RepositoryConfig config =
                new RepositoryConfig(Set.of("jar"), Map.of());

        assertThrows(UnsupportedOperationException.class,
                () -> config.allowedExtensions().add("zip"));
    }

    @Test
    void scopesCannotBeModified() {
        RepositoryConfig config =
                new RepositoryConfig(
                        Set.of("jar"),
                        Map.of("compile", true)
                );

        assertThrows(UnsupportedOperationException.class,
                () -> config.scopes().put("runtime", false));
    }
}

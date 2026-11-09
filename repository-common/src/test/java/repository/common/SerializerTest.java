package repository.common;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest {

    private final RepositoryConfig config = new RepositoryConfig(
            Set.of("jar", "zip"),
            Map.of("compile", true, "runtime", true, "test", false)
    );

    private final Dependency dependency1 = new Dependency(
            new ComponentId("org.example", "utils", "1.0.0"),
            "compile",
            false
    );

    private final Dependency dependency2 = new Dependency(
            new ComponentId("org.example", "logger", "2.0.0"),
            "runtime",
            true
    );

    // RepositoryConfig tests

    @Test
    void serializesConfig() {
        String json = Serializer.serializeConfig(config);

        var object = JsonParser.parseString(json).getAsJsonObject();

        assertTrue(object.has("allowedExtensions"));
        assertTrue(object.has("scopes"));
        assertTrue(object.getAsJsonObject("scopes")
                .get("compile").getAsBoolean());
        assertFalse(object.getAsJsonObject("scopes")
                .get("test").getAsBoolean());
    }

    @Test
    void deserializesConfig() {
        String json = """
                {
                    "allowedExtensions": ["jar", "zip"],
                    "scopes": {
                        "compile": true,
                        "test": false
                    }
                }
                """;

        RepositoryConfig result = Serializer.deserializeConfig(json);

        assertEquals(Set.of("jar", "zip"), result.allowedExtensions());
        assertEquals(
                Map.of("compile", true, "test", false),
                result.scopes()
        );
    }

    @Test
    void configRoundTrip() {
        String json = Serializer.serializeConfig(config);
        RepositoryConfig result = Serializer.deserializeConfig(json);

        assertEquals(config, result);
    }

    @Test
    void emptyConfigRoundTrip() {
        RepositoryConfig empty = new RepositoryConfig(
                Set.of(),
                Map.of()
        );

        String json = Serializer.serializeConfig(empty);
        RepositoryConfig result = Serializer.deserializeConfig(json);

        assertEquals(empty, result);
    }

    // Dependency tests

    @Test
    void serializesDependencies() {
        String json = Serializer.serializeDependencies(
                List.of(dependency1, dependency2)
        );

        var array = JsonParser.parseString(json).getAsJsonArray();

        assertEquals(2, array.size());
        assertEquals(
                "compile",
                array.get(0).getAsJsonObject().get("scope").getAsString()
        );
        assertTrue(
                array.get(1).getAsJsonObject()
                        .get("optional").getAsBoolean()
        );
    }

    @Test
    void deserializesDependencies() {
        String json = Serializer.serializeDependencies(
                List.of(dependency1, dependency2)
        );

        List<Dependency> result =
                Serializer.deserializeDependencies(json);

        assertEquals(2, result.size());
        assertInstanceOf(Dependency.class, result.get(0));
        assertEquals(dependency1, result.get(0));
        assertEquals(dependency2, result.get(1));
    }

    @Test
    void dependenciesRoundTrip() {
        List<Dependency> original = List.of(
                dependency1,
                dependency2
        );

        String json = Serializer.serializeDependencies(original);
        List<Dependency> result =
                Serializer.deserializeDependencies(json);

        assertEquals(original, result);
    }

    @Test
    void emptyDependenciesRoundTrip() {
        String json = Serializer.serializeDependencies(List.of());

        List<Dependency> result =
                Serializer.deserializeDependencies(json);

        assertEquals(List.of(), result);
    }

    @Test
    void deserializesEmptyDependencyArray() {
        List<Dependency> result =
                Serializer.deserializeDependencies("[]");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Invalid JSON tests

    @Test
    void rejectsMalformedConfigJson() {
        String json = "{\"allowedExtensions\": [";

        assertThrows(JsonParseException.class,
                () -> Serializer.deserializeConfig(json));
    }

    @Test
    void rejectsMalformedDependenciesJson() {
        String json = "[{\"id\":";

        assertThrows(JsonParseException.class,
                () -> Serializer.deserializeDependencies(json));
    }
}

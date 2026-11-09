package repository.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryMetadataTest {

    @TempDir
    Path tempDir;

    @Test
    void configWriteThenReadReturnsSame() {
        RepositoryConfig config = new RepositoryConfig(
                Set.of("jar", "zip"),
                Map.of("compile", true, "test", false)
        );
        Path file = RepositoryLayout.configFile(tempDir);

        RepositoryMetadata.writeConfig(file, config);
        RepositoryConfig read = RepositoryMetadata.readConfig(file);

        assertEquals(config.allowedExtensions(), read.allowedExtensions());
        assertEquals(config.scopes(), read.scopes());
    }

    @Test
    void dependenciesWriteThenReadReturnsSame() {
        ComponentId owner = new ComponentId("acme", "core", "1.0");
        List<Dependency> dependencies = List.of(
                new Dependency(new ComponentId("acme", "utils", "1.5"), "compile", false),
                new Dependency(new ComponentId("acme", "logging", "2.1"), "test", true)
        );
        Path file = RepositoryLayout.metadataFile(tempDir, owner);

        RepositoryMetadata.writeDependencies(file, dependencies);

        assertEquals(dependencies, RepositoryMetadata.readDependencies(file));
    }

    @Test
    void emptyDependenciesWork() {
        Path file = tempDir.resolve("x").resolve("component.json");

        RepositoryMetadata.writeDependencies(file, List.of());

        assertEquals(List.of(), RepositoryMetadata.readDependencies(file));
    }
}
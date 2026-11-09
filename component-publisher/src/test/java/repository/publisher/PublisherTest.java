
package repository.publisher;

import repository.common.Component;
import repository.common.Dependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.common.ComponentId;
import repository.common.RepositoryConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PublisherTest {

    @TempDir
    Path tempDir;

    private InMemoryRepositoryStorage storage;
    private Publisher publisher;

    private final RepositoryConfig config = new RepositoryConfig(
            Set.of("jar", "zip"),
            Map.of("compile", true, "runtime", true, "test", false)
    );

    private final ComponentId componentId =
            new ComponentId("org.example", "library", "1.0.0");

    private final ComponentId dependencyId =
            new ComponentId("org.example", "utils", "2.0.0");

    @BeforeEach
    void setUp() {
        storage = new InMemoryRepositoryStorage();
        publisher = new Publisher(storage);
        publisher.create("test-repository", config);
    }

    private Path createArtifact(String filename) throws IOException {
        Path artifact = tempDir.resolve(filename);
        Files.write(artifact, new byte[]{0, 1, 2, (byte) 255});
        return artifact;
    }

    private Component createComponent() {
        return new Component(componentId, List.of());
    }

    @Test
    void createsRepositoryWithConfiguration() {
        assertTrue(storage.exists("config.json"));

        assertEquals(config, publisher.getConfig());

        String json = new String(
                storage.getBytes("config.json"),
                StandardCharsets.UTF_8
        );

        assertEquals(config, Serializer.deserializeConfig(json));
    }

    @Test
    void opensExistingRepository() {
        Publisher anotherPublisher = new Publisher(storage);

        anotherPublisher.open("test-repository");

        assertEquals(config, anotherPublisher.getConfig());
    }

    @Test
    void publishesComponent() throws IOException {
        Path artifact = createArtifact("library.jar");

        publisher.publish(createComponent(), artifact);

        String directory = "org.example/library/1.0.0";

        assertTrue(storage.exists(directory));
        assertTrue(storage.exists(directory + "/library.jar"));
        assertTrue(storage.exists(directory + "/dependencies.json"));

        assertArrayEquals(
                Files.readAllBytes(artifact),
                storage.getBytes(directory + "/library.jar")
        );

        String json = new String(
                storage.getBytes(directory + "/dependencies.json"),
                StandardCharsets.UTF_8
        );

        assertEquals(
                List.of(),
                Serializer.deserializeDependencies(json)
        );
    }

    @Test
    void rejectsDuplicateComponent() throws IOException {
        Path artifact = createArtifact("library.jar");
        Component component = createComponent();

        publisher.publish(component, artifact);

        assertThrows(IllegalArgumentException.class,
                () -> publisher.publish(component, artifact));
    }

    @Test
    void rejectsUnsupportedExtension() throws IOException {
        Path artifact = createArtifact("library.txt");

        assertThrows(IllegalArgumentException.class,
                () -> publisher.publish(createComponent(), artifact));

        assertFalse(storage.exists("org.example/library/1.0.0"));
    }

    @Test
    void rejectsArtifactWithoutExtension() throws IOException {
        Path artifact = createArtifact("library");

        assertThrows(IllegalArgumentException.class,
                () -> publisher.publish(createComponent(), artifact));
    }

    @Test
    void rejectsMissingDependency() throws IOException {
        Dependency dependency =
                new Dependency(dependencyId, "compile", false);

        Component component = new Component(
                componentId,
                List.of(dependency)
        );

        Path artifact = createArtifact("library.jar");

        assertThrows(IllegalArgumentException.class,
                () -> publisher.publish(component, artifact));

        assertFalse(storage.exists("org.example/library/1.0.0"));
    }

    @Test
    void rejectsUnsupportedScope() throws IOException {
        Dependency dependency =
                new Dependency(dependencyId, "unknown", false);

        Component component = new Component(
                componentId,
                List.of(dependency)
        );

        Path artifact = createArtifact("library.jar");

        assertThrows(IllegalArgumentException.class,
                () -> publisher.publish(component, artifact));
    }

    @Test
    void publishesComponentWithExistingDependency() throws IOException {
        Component dependencyComponent =
                new Component(dependencyId, List.of());

        publisher.publish(
                dependencyComponent,
                createArtifact("utils.jar")
        );

        Dependency dependency =
                new Dependency(dependencyId, "compile", false);

        Component component = new Component(
                componentId,
                List.of(dependency)
        );

        publisher.publish(component, createArtifact("library.jar"));

        String json = new String(
                storage.getBytes(
                        "org.example/library/1.0.0/dependencies.json"
                ),
                StandardCharsets.UTF_8
        );

        assertEquals(
                List.of(dependency),
                Serializer.deserializeDependencies(json)
        );
    }

    @Test
    void acceptsExistingOptionalDependency() throws IOException {
        publisher.publish(
                new Component(dependencyId, List.of()),
                createArtifact("utils.jar")
        );

        Dependency optionalDependency =
                new Dependency(dependencyId, "runtime", true);

        Component component = new Component(
                componentId,
                List.of(optionalDependency)
        );

        publisher.publish(component, createArtifact("library.jar"));

        assertTrue(storage.exists(
                "org.example/library/1.0.0/library.jar"
        ));
    }

    @Test
    void rejectsDirectoryAsArtifact() {
        assertThrows(IllegalArgumentException.class,
                () -> publisher.publish(createComponent(), tempDir));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class,
                () -> publisher.publish(null, tempDir));

        assertThrows(NullPointerException.class,
                () -> publisher.publish(createComponent(), null));
    }

    // Fake storage implementation for testing

    private static class InMemoryRepositoryStorage
            implements RepositoryStorage {

        private static class RepositoryData {
            final Map<String, byte[]> files = new HashMap<>();
            final Set<String> directories = new HashSet<>(Set.of(""));
        }

        private final Map<String, RepositoryData> repositories =
                new HashMap<>();

        private RepositoryData current;

        private RepositoryData repository() {
            if (current == null) {
                throw new IllegalStateException("No repository open");
            }
            return current;
        }

        @Override
        public void createRepositoryAt(String location) {
            if (repositories.containsKey(location)) {
                throw new IllegalStateException(
                        "Repository already exists: " + location
                );
            }

            current = new RepositoryData();
            repositories.put(location, current);
        }

        @Override
        public void openRepositoryAt(String location) {
            RepositoryData found = repositories.get(location);

            if (found == null) {
                throw new IllegalArgumentException(
                        "Repository not found: " + location
                );
            }

            current = found;
        }

        @Override
        public boolean exists(String relativePath) {
            return repository().files.containsKey(relativePath)
                    || repository().directories.contains(relativePath);
        }

        @Override
        public List<String> list(String directory) {
            RepositoryData repo = repository();

            String prefix = directory.isEmpty()
                    ? ""
                    : directory + "/";

            Set<String> paths = new HashSet<>(repo.directories);
            paths.addAll(repo.files.keySet());

            Set<String> children = new TreeSet<>();

            for (String path : paths) {
                if (!path.startsWith(prefix) || path.equals(directory)) {
                    continue;
                }

                String remainder = path.substring(prefix.length());

                if (!remainder.isEmpty()) {
                    children.add(remainder.split("/", 2)[0]);
                }
            }

            return new ArrayList<>(children);
        }

        @Override
        public InputStream read(String relativePath) {
            byte[] bytes = repository().files.get(relativePath);

            if (bytes == null) {
                throw new IllegalArgumentException(
                        "File not found: " + relativePath
                );
            }

            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void createDirectories(String path) {
            String currentPath = "";

            for (String segment : path.split("/")) {
                currentPath = currentPath.isEmpty()
                        ? segment
                        : currentPath + "/" + segment;

                repository().directories.add(currentPath);
            }
        }

        @Override
        public void writeNew(String relativePath, InputStream content) {
            if (exists(relativePath)) {
                throw new IllegalStateException(
                        "File already exists: " + relativePath
                );
            }

            int slash = relativePath.lastIndexOf('/');
            String parent = slash < 0
                    ? ""
                    : relativePath.substring(0, slash);

            if (!repository().directories.contains(parent)) {
                throw new IllegalStateException(
                        "Parent directory does not exist: " + parent
                );
            }

            try {
                repository().files.put(
                        relativePath,
                        content.readAllBytes()
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void overwrite(String relativePath, InputStream content) {
            if (!repository().files.containsKey(relativePath)) {
                throw new IllegalArgumentException(
                        "File not found: " + relativePath
                );
            }

            try {
                repository().files.put(
                        relativePath,
                        content.readAllBytes()
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        byte[] getBytes(String relativePath) {
            byte[] bytes = repository().files.get(relativePath);

            if (bytes == null) {
                throw new IllegalArgumentException(
                        "File not found: " + relativePath
                );
            }

            return bytes.clone();
        }
    }
}

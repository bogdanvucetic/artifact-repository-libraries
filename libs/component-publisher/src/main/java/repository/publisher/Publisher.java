package repository.publisher;

import repository.common.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

// todo
//  - create/open/publish -> createRepository(), openRepository(), publishToRepository()
public class Publisher {
    // todo:
    //  - create() bez konfig fajla overload?
    //  - da li ima još šta da se overload-uje?

    private final RepositoryWriter storage;
    private RepositoryConfig config;

    public Publisher(RepositoryWriter storage) {
        this.storage = Objects.requireNonNull(storage, "Storage cannot be null");
    }

    public void create(String location, RepositoryConfig config) {
        Objects.requireNonNull(config, "Configuration cannot be null");
        validateConfig(config);

        this.config = null;
        storage.createRepositoryAt(location);

        String json = Serializer.serializeConfig(config);

        try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            storage.writeNew("config.json", stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write repository configuration", e);
        }

        this.config = config;
    }

    public void open(String location) {
        this.config = null;
        storage.openRepositoryAt(location);
        loadConfig();
    }

    /**
     * Will not publish if a component with that particular organization, name and version already exists
     * @param component
     * @param artifactPath
     */
    public void publish(Component component, Path artifactPath) {
        Objects.requireNonNull(component, "Component cannot be null");
        Objects.requireNonNull(artifactPath, "Artifact path cannot be null");

        loadConfig();
        validatePublication(component, artifactPath);

        String directory = componentDirectory(component.id());
        storage.createDirectories(directory);

        try (InputStream stream = Files.newInputStream(artifactPath)) {
            storage.writeNew(directory + "/" + artifactPath.getFileName().toString(), stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write component", e);
        }

        String json = Serializer.serializeDependencies(component.dependencies());
        try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            storage.writeNew(directory + "/dependencies.json", stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dependencies.json", e);
        }
    }

    private void loadConfig() {
        try (InputStream stream = storage.read("config.json")) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            RepositoryConfig loadedConfig = Serializer.deserializeConfig(json);

            validateConfig(loadedConfig);
            config = loadedConfig;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read repository configuration", e);
        }
    }

    // todo: eventualno za sve te provere
    //  - može npr. da ne prihvata "/", "." i sl. karaktere za ekstenzije
    //  - pa da scope-ovi nisu " "
    //  - da je neophodna makar jedna ekstenzija
    // mada ovo možda može i u RepositoryConfig konstruktor
    private void validateConfig(RepositoryConfig config) {

    }

    private void validatePublication(Component component, Path artifactPath) {
        if (!Files.isRegularFile(artifactPath)) {
            throw new IllegalArgumentException("Invalid artifact path");
        }

        String fileName = artifactPath.getFileName().toString();
        int dot = fileName.lastIndexOf('.');

        if (dot <= 0 || dot == fileName.length() - 1) {
            throw new IllegalArgumentException("Invalid file extension");
        }

        String extension = fileName.substring(dot + 1);

        if (!config.allowedExtensions().contains(extension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }

        if (fileName.equals("dependencies.json")) {
            throw new IllegalArgumentException("Artifact name conflicts with metadata file");
        }

        String directory = componentDirectory(component.id());

        if (storage.exists(directory)) {
            throw new IllegalArgumentException("Component already exists: " + component.id());
        }

        for (Dependency dependency : component.dependencies()) {
            if (!config.scopes().containsKey(dependency.scope())) {
                throw new IllegalArgumentException("Unsupported scope: " + dependency.scope());
            }

            String dependencyPath = componentDirectory(dependency.id()) + "/dependencies.json";

            if (!storage.exists(dependencyPath)) {
                throw new IllegalArgumentException("Missing dependency: " + dependency.id());
            }
        }
    }

    private static String componentDirectory(ComponentId id) {
        return id.organization() + "/" +
                id.name() + "/" +
                id.version();
    }

    public RepositoryConfig getConfig() {
        return config;
    }

    // todo: sve navedene list metode
}

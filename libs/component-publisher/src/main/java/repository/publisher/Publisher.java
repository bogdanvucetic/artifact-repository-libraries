package repository.publisher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import repository.common.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class Publisher {
    private final Storage storage;
    private RepositoryConfig config;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Publisher(Storage storage) {
        this.storage = Objects.requireNonNull(storage, "Storage cannot be null");
    }

    public void createRepository(String location, RepositoryConfig config) {
        Objects.requireNonNull(config, "Configuration cannot be null");
        validateConfig(config);

        this.config = null;
        storage.createRepositoryAt(location);

        String json = gson.toJson(config);

        try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            storage.createFile("config.json", stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write repository configuration", e);
        }

        this.config = config;
    }

    public void openRepository(String location) {
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
            storage.createFile(directory + "/" + artifactPath.getFileName().toString(), stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write component", e);
        }

        String json = gson.toJson(component.dependencies());
        try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            storage.createFile(directory + "/dependencies.json", stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dependencies.json", e);
        }
    }

    public List<String> listAllOrganizations() {
        return storage.listDirectories("");
    }

    public List<String> listAllProjectsInOrganization(String organization) {
        Objects.requireNonNull(organization, "Organizacija ne moze biti null");

        if (organization.isBlank())
            throw new RepositoryException("Naziv organizacije ne moze biti prazan");

        return storage.listDirectories(organization);
    }

    public List<String> listAllProjectsInOrganization(ComponentId id) {
        Objects.requireNonNull(id, "ID komponente ne moze biti null");
        return listAllProjectsInOrganization(id.organization());
    }

    public List<String> listAllProjectVersions(String organization, String name) {
        Objects.requireNonNull(organization, "Organizacija ne moze biti null");
        Objects.requireNonNull(name, "Naziv projekta ne moze biti null");

        if (organization.isBlank())
            throw new RepositoryException("Naziv organizacije ne moze biti prazan");

        if (name.isBlank())
            throw new RepositoryException("Naziv projekta ne moze biti prazan");

        return storage.listDirectories(organization + "/" + name);
    }

    public List<String> listAllProjectVersions(ComponentId id) {
        Objects.requireNonNull(id, "ID komponente ne moze biti null");
        return listAllProjectVersions(id.organization(), id.name());
    }

    public List<Dependency> listAllProjectDependencies(String organization, String name, String version) {
        return listAllProjectDependencies(new ComponentId(organization, name, version));
    }

    public List<Dependency> listAllProjectDependencies(ComponentId id) {
        Objects.requireNonNull(id, "ID komponente ne moze biti null");

        String path = componentDirectory(id) + "/dependencies.json";

        if (!storage.exists(path))
            throw new RepositoryException("Komponenta nije pronadjena: " + id);

        try (InputStream stream = storage.read(path)) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            List<Dependency> dependencies = gson.fromJson(json,
                    new TypeToken<List<Dependency>>() {}.getType());

            if (dependencies == null)
                throw new RepositoryException("Nevalidan dependencies.json: " + id);

            return new Component(id, dependencies).dependencies();
        } catch (Exception e) {
            throw new RepositoryException("Neuspesno citanje zavisnosti: " + id, e);
        }
    }

    private void loadConfig() {
        try (InputStream stream = storage.read("config.json")) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            RepositoryConfig loadedConfig = gson.fromJson(json, RepositoryConfig.class);

            validateConfig(loadedConfig);
            config = loadedConfig;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read repository configuration", e);
        }
    }

    private void validateConfig(RepositoryConfig config) {
        if (config == null)
            throw new RepositoryException("Nevalidan config.json");

        new RepositoryConfig(config.allowedExtensions(), config.scopes());
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
}

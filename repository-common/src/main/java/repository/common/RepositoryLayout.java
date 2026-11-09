package repository.common;

import java.nio.file.Path;

public final class RepositoryLayout {

    public static final String CONFIG_FILE = "repository-config.json";
    public static final String METADATA_FILE = "component.json";

    private RepositoryLayout() {
    }

    public static Path configFile(Path root) {
        return root.resolve(CONFIG_FILE);
    }

    public static Path organizationDir(Path root, String organization) {
        return root.resolve(organization);
    }

    public static Path projectDir(Path root, String organization, String name) {
        return organizationDir(root, organization).resolve(name);
    }

    public static Path componentDir(Path root, ComponentId id) {
        return projectDir(root, id.organization(), id.name()).resolve(id.version());
    }

    public static Path metadataFile(Path root, ComponentId id) {
        return componentDir(root, id).resolve(METADATA_FILE);
    }

    public static Path artifactFile(Path root, ComponentId id, String extension) {
        return componentDir(root, id).resolve(id.name() + "-" + id.version() + "." + extension);
    }
}
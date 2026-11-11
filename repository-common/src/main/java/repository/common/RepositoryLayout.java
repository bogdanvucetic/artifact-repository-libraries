package repository.common;

public final class RepositoryLayout {


    // utility klasa koja definiše stvari kao CONFIG_FILE ime, METADATA_FILE ime, regeks obrazac sa kojim se
    // poklapa ime komponente?

    public static final String CONFIG_FILE = "repository-config.json";
    public static final String METADATA_FILE = "component.json";
    public static final String ROOT = "";

    private RepositoryLayout() {
    }

    public static String configFile() {
        return CONFIG_FILE;
    }

    public static String organizationDir(String organization) {
        return organization;
    }

    public static String projectDir(String organization, String name) {
        return organization + "/" + name;
    }

    public static String componentDir(ComponentId id) {
        return projectDir(id.organization(), id.name()) + "/" + id.version();
    }

    public static String metadataFile(ComponentId id) {
        return componentDir(id) + "/" + METADATA_FILE;
    }

    public static String artifactFileName(ComponentId id, String extension) {
        return id.name() + "-" + id.version() + "." + extension;
    }

    public static String artifactFile(ComponentId id, String extension) {
        return componentDir(id) + "/" + artifactFileName(id, extension);
    }
}
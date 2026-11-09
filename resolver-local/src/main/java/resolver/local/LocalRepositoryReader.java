package resolver.local;

import repository.common.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalRepositoryReader implements RepositoryReader {

    private final Path root;

    public LocalRepositoryReader(Path root) {
        if (!Files.isDirectory(root)) {
            throw new RuntimeException("Repository does not exist: " + root);
        }
        this.root = root;
    }

    @Override
    public RepositoryConfig readConfig() {
        Path configFile = RepositoryLayout.configFile(root);
        if (!Files.exists(configFile)) {
            throw new RuntimeException("Repository config not found: " + configFile);
        }

        return RepositoryMetadata.readConfig(configFile);
    }

    @Override
    public List<String> listOrganizations() {
        return listFolders(root.toFile());
    }

    @Override
    public List<String> listProjects(String organization) {
        File dir = RepositoryLayout.organizationDir(root, organization).toFile();
        return listFolders(dir);
    }

    @Override
    public List<String> listVersions(String organization, String name) {
        File dir = RepositoryLayout.projectDir(root, organization, name).toFile();
        return listFolders(dir);
    }

    @Override
    public Component readComponent(ComponentId id) {
        Path metadataFile = RepositoryLayout.metadataFile(root, id);
        if (!Files.exists(metadataFile)) {
            throw new RuntimeException("Component not found: " + id);
        }
        List<Dependency> dependencies = RepositoryMetadata.readDependencies(metadataFile);
        return new Component(id, dependencies);
    }

    @Override
    public Path download(ComponentId id, Path targetDir) {
        Path componentDir = RepositoryLayout.componentDir(root, id);
        File artifact = findArtifact(componentDir.toFile(), id);

        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(artifact.getName());
            Files.copy(artifact.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new RuntimeException("Error downloading component: " + id, e);
        }
    }

    @Override
    public void close() {
    }

    // fajl komponente je jedini fajl u folderu verzije koji nije component.json
    private File findArtifact(File dir, ComponentId id) {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new RuntimeException("Component not found: " + id);
        }
        for (File file : files) {
            if (file.isFile() && !file.getName().equals(RepositoryLayout.METADATA_FILE)) {
                return file;
            }
        }
        throw new RuntimeException("Component file not found: " + id);
    }

    // vraca imena podfoldera, sortirana
    private List<String> listFolders(File dir) {
        List<String> result = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.add(file.getName());
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public void openRepositoryAt(String location) {

    }

    @Override
    public boolean exists(String relativePath) {
        return false;
    }

    @Override
    public List<String> list(String directory) {
        return List.of();
    }

    @Override
    public InputStream read(String relativePath) {
        return null;
    }
}
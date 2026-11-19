package resolver.api;

import repository.common.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: OBRISATI
public class DefaultRepositoryStorage implements RepositoryStorage {

    private final RepositoryReader reader;

    public DefaultRepositoryStorage(RepositoryReader reader) {
        this.reader = reader;
    }

    @Override
    public RepositoryConfig readConfig() {
        String path = RepositoryLayout.configFile();
        if (!reader.exists(path)) {
            throw new RepositoryException("Repository config not found");
        }
        return Serializer.deserializeConfig(readText(path));
    }

    @Override
    public List<String> listOrganizations() {
        return sorted(reader.listDirectories(RepositoryLayout.ROOT));
    }

    @Override
    public List<String> listProjects(String organization) {
        return sorted(reader.listDirectories(RepositoryLayout.organizationDir(organization)));
    }

    @Override
    public List<String> listVersions(String organization, String name) {
        return sorted(reader.listDirectories(RepositoryLayout.projectDir(organization, name)));
    }

    @Override
    public Component readComponent(ComponentId id) {
        String path = RepositoryLayout.metadataFile(id);
        if (!reader.exists(path)) {
            throw new RepositoryException("Component not found: " + id);
        }
        List<Dependency> dependencies = Serializer.deserializeDependencies(readText(path));
        return new Component(id, dependencies == null ? List.of() : dependencies);
    }

    @Override
    public Path download(ComponentId id, Path targetDir) {
        String fileName = findArtifactName(id);
        String source = RepositoryLayout.componentDir(id) + "/" + fileName;

        try (InputStream in = reader.read(source)) {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(fileName);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new RepositoryException("Error downloading component: " + id, e);
        }
    }

    @Override
    public void close() {
        reader.close();
    }

    // fajl komponente je jedini fajl u folderu verzije koji nije component.json
    private String findArtifactName(ComponentId id) {
        String dir = RepositoryLayout.componentDir(id);
        if (!reader.exists(dir)) {
            throw new RepositoryException("Component not found: " + id);
        }
        for (String name : reader.listFiles(dir)) {
            if (!name.equals(RepositoryLayout.METADATA_FILE)) {
                return name;
            }
        }
        throw new RepositoryException("Component file not found: " + id);
    }

    private String readText(String path) {
        try (InputStream in = reader.read(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Error reading " + path, e);
        }
    }

    private static List<String> sorted(List<String> names) {
        List<String> result = new ArrayList<>(names);
        Collections.sort(result);
        return result;
    }
}
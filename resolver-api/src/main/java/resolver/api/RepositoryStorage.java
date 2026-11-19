package resolver.api;

import repository.common.Component;
import repository.common.ComponentId;
import repository.common.RepositoryConfig;

import java.nio.file.Path;
import java.util.List;

// TODO: OBRISATI
public interface RepositoryStorage extends AutoCloseable {
    RepositoryConfig readConfig();

    List<String> listOrganizations();
    List<String> listProjects(String organization);
    List<String> listVersions(String organization, String name);

    Component readComponent(ComponentId id);

    Path download(ComponentId id, Path targetDir);

    @Override
    void close();
}
package org.example.resolver.api;

import org.example.repository.common.Component;
import org.example.repository.common.ComponentId;
import org.example.repository.common.RepositoryConfig;

import java.nio.file.Path;
import java.util.List;

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
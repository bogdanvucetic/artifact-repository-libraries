package resolver.api;

import repository.common.ComponentId;

import java.util.List;
import java.nio.file.Path;

public interface DependencyResolver {

    ResolutionResult resolve(List<ComponentId> roots, ResolveOptions options);
    ResolutionResult resolveFromFile(Path componentsFile, ResolveOptions options);
}
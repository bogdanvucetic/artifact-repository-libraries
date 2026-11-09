package resolver.local;

import resolver.api.RepositoryStorage;
import resolver.api.ResolverProvider;

import java.nio.file.Path;
import java.util.Map;

public class LocalResolverProvider implements ResolverProvider {

    @Override
    public String name() {
        return "local";
    }

    @Override
    public RepositoryStorage open(String location, Map<String, String> options) {
        return new LocalRepositoryStorage(Path.of(location));
    }
}
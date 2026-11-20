package resolver.local;

import repository.common.RepositoryReader;
import resolver.api.ResolverProvider;

import java.nio.file.Path;
import java.util.Map;

// todo:
//  - obrisali i spojili u jedan StorageProvider u common
public class LocalResolverProvider implements ResolverProvider {

    @Override
    public String name() {
        return "local";
    }

    @Override
    public RepositoryReader open(String location, Map<String, String> options) {
        return new LocalRepositoryReader(Path.of(location));
    }
}
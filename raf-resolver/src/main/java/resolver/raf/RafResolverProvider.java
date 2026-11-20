package resolver.raf;

import repository.common.RepositoryReader;
import resolver.api.ResolverProvider;

import java.util.Map;

public class RafResolverProvider implements ResolverProvider {
    @Override
    public String name() {
        return "raf";
    }

    @Override
    public RepositoryReader open(String location, Map<String, String> options) {
        return new RafRepositoryReader("sftp.example.invalid", "REDACTED_USER", "REDACTED_PASSWORD", location);
    }
}

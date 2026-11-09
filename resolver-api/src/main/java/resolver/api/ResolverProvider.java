package resolver.api;

import repository.common.RepositoryReader;

import java.util.Map;

public interface ResolverProvider {
    String name();
    RepositoryReader open(String location, Map<String, String> options);
}
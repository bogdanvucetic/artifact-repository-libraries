package org.example.resolver.api;

import java.util.Map;

public interface ResolverProvider {
    String name();
    RepositoryStorage open(String location, Map<String, String> options);
}
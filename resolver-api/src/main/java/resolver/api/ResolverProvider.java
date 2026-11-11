package resolver.api;

import repository.common.RepositoryReader;

import java.util.Map;

public interface ResolverProvider {

    // misli se na ime driver npr. local itd
    String name();

    // Otvara postojeći repozitorijum na datoj lokaciji
    RepositoryReader open(String location, Map<String, String> options);
}
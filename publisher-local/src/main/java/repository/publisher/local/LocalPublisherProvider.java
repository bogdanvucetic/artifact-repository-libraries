package repository.publisher.local;

import repository.common.RepositoryWriter;
import repository.publisher.PublisherProvider;

import java.util.Map;

// lokalni drajver za publisher, ucitava se preko META-INF/services
public class LocalPublisherProvider implements PublisherProvider {

    @Override
    public String name() {
        return "local";
    }

    @Override
    public RepositoryWriter newWriter(Map<String, String> options) {
        // local ne koristi nikakve opcije, al ce da treba
        return new LocalRepositoryWriter();
    }
}
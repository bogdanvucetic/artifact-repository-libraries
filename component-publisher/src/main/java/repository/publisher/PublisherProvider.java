package repository.publisher;

import repository.common.RepositoryWriter;

import java.util.Map;
import java.util.ServiceLoader;

/* bogdanele, ovo sam dodao jer nam fali SPI
ovo samo služi da CLI dobije pravi writer (local/mreža/drive) a da ne zna konkretnu klasu,
isto kao ResolverProvider kod resolvera. lokalna implementacija je u publisher-local.
vidi da l ti okej pa da cepamo dalje */

public interface PublisherProvider {

    // ime drajvera samo da l je drive local itd
    String name();

    RepositoryWriter newWriter(Map<String, String> options);

    // nadje drajver po imenu iz META-INF/services
    static PublisherProvider load(String name) {
        for (PublisherProvider provider : ServiceLoader.load(PublisherProvider.class)) {
            if (provider.name().equals(name)) {
                return provider;
            }
        }
        throw new RuntimeException("Nema publisher drajvera: " + name);
    }
}
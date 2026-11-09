package org.example.repository.publisher;

import org.example.repository.common.Component;
import org.example.repository.common.ComponentId;
import org.example.repository.common.Dependency;
import org.example.repository.common.RepositoryConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class Publisher {
    private final RepositoryStorage storage;

    public Publisher(RepositoryStorage storage) {
        this.storage = Objects.requireNonNull(storage, "Storage cannot be null");
    }

    public void create(String location, RepositoryConfig config) {
        // uradi provere da je ovo validna konfiguracija

        // write(novi folder na lokaciji)
        // serijalizuj i write novi config fajl na lokaciji
        // stavi da ovo bude state storage-a ili sl., da je tu fokusiran
    }

    public void open(String location) {
        // stavi da ovo bude state storage-a ili sl., da je tu fokusiran
        // učitaj i deserijalizuj config folder

        // uradi provere da je ovo validan repoziyorijum
    }

    public RepositoryConfig readConfig() {
        // konfig fajl putanja se dobija iz fokusiran folder.config
        // storage.read(trazeni konfig fajl putanja)
        // deserijalizuj
        // proveri da je validan
        // vrati objekat
        return null;
    }

    public List<Dependency> readDependencies(ComponentId id) {
        // rekonstruiši logički relativePath iz componentId

        // storage.read(trazeni dependencies fajl
        // deserijalizuj i vrati objekat
        return null;
    }

    /**
     * Will not publish if a component with that particular organization, name and version already exists
     * @param component
     * @param artifact
     */
    public void publish(Component component, Path artifact) {
        // read repoconfig
        // validate that extensions may be published to repository
        // validate that scopes of dependencies are valid

        // check that componentId is unused, tj. reject publish ako već postoji ta verzija i sl., ako postoji taj organization.name.version folder uopšte

        // check all dependencies exist (in repository?)

        // šta kada već postoji taj fajl/putanja?

        // serialize the component.dependencies into a json
        // and then storage.store() for that file and all other files
        // storage.store(fajl konvertovan u inputstream);
    }

    // todo: sve navedene list metode

    public RepositoryStorage getStorage() {
        return storage;
    }
}

package repository.common;

import java.io.InputStream;
import java.util.List;

public interface RepositoryReader {
    // todo: polja:
    //  - putanja/štagod do trenutno otvorenog repozitorijuma
    //  - RepositoryConfig?

    // todo: šta da se desi ako ne postoji? neki izuzetak?
    void openRepositoryAt(String location);

    // todo: biće korisno npr. da proverimo da sve zavisnosti postoje pre nego što publish-ujemo
    boolean exists(String relativePath);

    // todo: ili tipa showContentAt()
    List<String> list(String directory);

    // todo: on će se koristiti za readConfig() i readDependencies(), metode koje implementira RepositoryStorage
    //  download() umesto read()?
    InputStream read(String relativePath);
}

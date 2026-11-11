package repository.common;

import java.io.InputStream;
import java.util.List;

public interface RepositoryReader extends AutoCloseable{
    // todo: polja:
    //  - putanja/štagod do trenutno otvorenog repozitorijuma
    //  - RepositoryConfig?

    // todo: šta da se desi ako ne postoji? neki izuzetak?
    //   void openRepositoryAt(String location);

    // todo: biće korisno npr. da proverimo da sve zavisnosti postoje pre nego što publish-ujemo
    boolean exists(String relativePath);

    List<String> listDirectories(String relativePath);

    List<String> listFiles(String relativePath);

    // todo: on će se koristiti za readConfig() i readDependencies(), metode koje implementira RepositoryStorage
    //  download() umesto read()?
    InputStream read(String relativePath);

    @Override
    default void close(){
    }
}

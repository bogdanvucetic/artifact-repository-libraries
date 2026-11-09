package org.example.repository.publisher;

import java.io.InputStream;
import java.util.List;

public interface RepositoryStorage {
    // todo: polja:
    //  - putanja/štagod do trenutno otvorenog repozitorijuma
    //  - RepositoryConfig?

    // ova dva će da budu ti koji zapravo fokusiraju storage na neki folder
    void createRepositoryAt(String location);

    // todo: šta da se desi ako ne postoji? neki izuzetak?
    void openRepositoryAt(String location);

    // todo: biće korisno npr. da proverimo da sve zavisnosti postoje pre nego što publish-ujemo
    boolean exists(String relativePath);

    List<String> list(String directory);

    // on će se koristiti za readConfig() i readDependencies(), metode koje implementira RepositoryStorage
    InputStream read(String relativePath);

    // potrebno za publish()
    void createDirectories(String path);

    // store and publish operations:
    // todo: should refuse to overwrite an existing file
    // KASNIJE: neki mehanizam koji parcijalno urađeni publication unazaduje na originalno
    //  - tipa writeAll? i tu onda može elegantno "ako ne uspeš sve, onda obriši sve urađeno"
    // todo: neko će morati da pravi direktorijume, pitanje samo da li writeNew ili tipa createFolder() ili ko zna šta
    void writeNew(String relativePath, InputStream content);

    // ovo bi bilo korisno tipa za neku updateConfig() metodu ako treba. nzm. da li treba tho
    void overwrite(String relativePath, InputStream content);
}

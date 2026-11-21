package repository.common;

import java.io.InputStream;
import java.util.List;

// todo:
//  - promenimo ime u StorageReader da bi isli zajedno StorageReader, StorageWriter i
//    StorageProvider
//  - da imamo samo jedan Storage koji je R/W, pa ako necemo da podrzimo write metode
//    (npr. za  resolver-a), samo da stavimo throws new UnsupportedOperation ili tako nesto?
//    ako nas briga malo za interface segregation
//  - vrv. nam ovo AutoCloseable ne treba ovde, tj. u svakom resursu, nego samo u Google Drive
//    i RAF-u i tamo gde je vec potrebno

// TODO MORA:
//  - sve metode za list i sl. ovde, kao default implementacije
//    (IPAK U PUBLISHER I RESOLVER, NIJE IM OVDE MESTO, ovde gde su samo bejzik rw operacije)
//    one ce da delegiraju ovde
//    - tipa onda readFileContent(), readFolderContent() metode?

// todo:
//  - promenimo ime u StorageWriter da bi isli zajedno StorageReader, StorageWriter i
//    StorageProvider
//  - writeNew() -> createFile()
public interface Storage {
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

    // ova dva će da budu ti koji zapravo fokusiraju storage na neki folder
    void createRepositoryAt(String location);

    void openRepositoryAt(String location);
    // potrebno za publish()

    void createDirectories(String path);

    // store and publish operations:
    // todo: should refuse to overwrite an existing file
    // KASNIJE: neki mehanizam koji parcijalno urađeni publication unazaduje na originalno
    //  - tipa writeAll? i tu onda može elegantno "ako ne uspeš sve, onda obriši sve urađeno"
    // todo: neko će morati da pravi direktorijume, pitanje samo da li writeNew ili tipa createFolder() ili ko zna šta
    //  ime bolje tipa saveFile()?
    void writeNew(String relativePath, InputStream content);

    /**
     * DARKU POTREBNE METODE
     * readConfig()
     * readComponent() -> napravi komponentu od metapodataka
     * download()
     * findArtifact()
     * listFolders()        kao moj list()
     * -
     * bez koriscenja trenutno:
     * listOrganizations()
     * listProjects(String organization)
     * listVersions(String organization, String name)
     */

    // ovo bi bilo korisno tipa za neku updateConfig() metodu ako treba. nzm. da li treba tho
    // void overwrite(String relativePath, InputStream content);
}

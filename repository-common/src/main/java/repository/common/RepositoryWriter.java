package repository.common;

import java.io.InputStream;

public interface RepositoryWriter extends RepositoryReader {
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

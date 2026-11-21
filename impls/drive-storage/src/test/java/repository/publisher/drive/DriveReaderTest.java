package repository.publisher.drive;

import repository.common.Storage;

import java.util.Map;

// probni program za korak 2: otvara folder "sk-proba" iz koraka 1 preko providera
// i ispisuje sta reader vidi (folder je prazan, bitno je da nema greske)
public class DriveReaderTest {

    public static void main(String[] args) {
        Storage reader = new DriveStorageProvider().create(Map.of());

        reader.createRepositoryAt("sk-proba-2");
        System.out.println("Otvoren repozitorijum sk-proba");

        System.out.println("exists(\"\"): " + reader.exists(""));
        System.out.println("exists(\"nema/ovoga.json\"): " + reader.exists("nema/ovoga.json"));
        System.out.println("folderi: " + reader.listDirectories(""));
        System.out.println("fajlovi: " + reader.listFiles(""));

        try {
            reader.openRepositoryAt("ne-postoji-ovaj-folder");
        } catch (Exception e) {
            System.out.println("OK, ocekivana greska: " + e.getMessage());
        }
    }
}
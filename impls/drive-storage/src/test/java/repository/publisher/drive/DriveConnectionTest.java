package repository.publisher.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import resolver.drive.DriveConnection;

import java.util.List;

public class DriveConnectionTest {

    private static final String FOLDER = "application/vnd.google-apps.folder";

    public static void main(String[] args) throws Exception {
        Drive drive = DriveConnection.connect();
        System.out.println("Povezan na Google Drive");

        if (findFolders(drive, "sk-proba").isEmpty()) {
            File metadata = new File();
            metadata.setName("sk-proba");
            metadata.setMimeType(FOLDER);
            metadata.setParents(List.of("root"));

            File created = drive.files().create(metadata).setFields("id, name").execute();
            System.out.println("Napravljen folder: " + created.getName() + " (" + created.getId() + ")");
        }

        System.out.println("Folderi koje aplikacija vidi u korenu:");
        for (File f : findFolders(drive, null)) {
            System.out.println("  " + f.getName() + "  " + f.getId());
        }
    }

    private static List<File> findFolders(Drive drive, String name) throws Exception {
        String q = "'root' in parents and mimeType = '" + FOLDER + "' and trashed = false";
        if (name != null) {
            q += " and name = '" + name + "'";
        }
        FileList result = drive.files().list()
                .setQ(q)
                .setFields("files(id, name)")
                .execute();
        return result.getFiles();
    }
}
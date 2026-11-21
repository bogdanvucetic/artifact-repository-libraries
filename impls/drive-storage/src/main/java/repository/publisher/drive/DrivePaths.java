package repository.publisher.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import repository.common.RepositoryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// ova klasa prevodi nasu putanju  u ID na drive-u
public class DrivePaths {

    public static final String FOLDER_TYPE = "application/vnd.google-apps.folder";

    private final Drive drive;
    private final String rootId;

    // pamtimo vec pronadjene putanje jer je svaki poziv ka drive-u spor
    private final Map<String, String> cache = new HashMap<>();

    public DrivePaths(Drive drive, String rootId) {
        this.drive = drive;
        this.rootId = rootId;
        cache.put("", rootId);
    }

    public String getRootId() {
        return rootId;
    }

    // vraca ID fajla/foldera na datoj putanji ili null ako ne postoji
    public String findId(String relativePath) {
        if (cache.containsKey(relativePath)) {
            return cache.get(relativePath);
        }

        String currentId = rootId;
        String currentPath = "";

        for (String part : relativePath.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            currentPath = currentPath.isEmpty() ? part : currentPath + "/" + part;

            if (cache.containsKey(currentPath)) {
                currentId = cache.get(currentPath);
                continue;
            }

            currentId = findChild(currentId, part);
            if (currentId == null) {
                return null; // ne pamtimo null jer writer moze kasnije da napravi taj fajl
            }
            cache.put(currentPath, currentId);
        }

        return currentId;
    }

    // writer javlja kad napravi novi fajl/folder da ga ne trazimo ponovo
    public void remember(String relativePath, String id) {
        cache.put(relativePath, id);
    }

    public String findChild(String parentId, String name) {
        String q = "'" + parentId + "' in parents and name = '" + escape(name) + "' and trashed = false";
        List<File> found = query(q);
        return found.isEmpty() ? null : found.get(0).getId();
    }

    public List<File> listChildren(String folderId) {
        return query("'" + folderId + "' in parents and trashed = false");
    }

    public static boolean isFolder(File file) {
        return FOLDER_TYPE.equals(file.getMimeType());
    }

    public static String findRootFolder(Drive drive, String name) {
        String q = "'root' in parents and name = '" + escape(name) + "' and mimeType = '"
                + FOLDER_TYPE + "' and trashed = false";
        List<File> found = query(drive, q);

        if (found.size() > 1) {
            throw new RepositoryException("Na Drive-u postoji vise foldera sa imenom: " + name);
        }
        return found.isEmpty() ? null : found.get(0).getId();
    }

    private List<File> query(String q) {
        return query(drive, q);
    }

    private static List<File> query(Drive drive, String q) {
        List<File> result = new ArrayList<>();
        String pageToken = null;

        try {
            do {
                FileList page = drive.files().list()
                        .setQ(q)
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .setPageSize(1000)
                        .setPageToken(pageToken)
                        .execute();
                result.addAll(page.getFiles());
                pageToken = page.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            throw new RepositoryException("Greska pri radu sa Google Drive-om: " + e.getMessage(), e);
        }

        return result;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
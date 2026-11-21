package repository.publisher.drive;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import repository.common.RepositoryException;
import repository.common.RepositoryWriter;
import resolver.drive.DriveConnection;
import resolver.drive.DrivePaths;
import resolver.drive.DriveRepositoryReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DriveRepositoryWriter implements RepositoryWriter {

    private final Drive drive;
    private DriveRepositoryReader reader;

    public DriveRepositoryWriter() {
        this.drive = DriveConnection.connect();
    }

    @Override
    public void createRepositoryAt(String location) {
        if (DrivePaths.findRootFolder(drive, location) != null) {
            throw new RepositoryException("Repozitorijum vec postoji na Drive-u: " + location);
        }

        String rootId = createFolder(location, "root");
        reader = new DriveRepositoryReader(drive, rootId);
    }

    @Override
    public void openRepositoryAt(String location) {
        String rootId = DrivePaths.findRootFolder(drive, location);
        if (rootId == null) {
            throw new RepositoryException("Repozitorijum ne postoji na Drive-u: " + location);
        }
        reader = new DriveRepositoryReader(drive, rootId);
    }

    @Override
    public void createDirectories(String path) {
        DrivePaths paths = paths();

        String parentId = paths.getRootId();
        String currentPath = "";

        for (String part : path.split("/")) {
            if (part.isEmpty()) {
                continue;
            }
            currentPath = currentPath.isEmpty() ? part : currentPath + "/" + part;

            String id = paths.findId(currentPath);
            if (id == null) {
                id = createFolder(part, parentId);
                paths.remember(currentPath, id);
            }
            parentId = id;
        }
    }

    @Override
    public void writeNew(String relativePath, InputStream content) {
        DrivePaths paths = paths();

        if (paths.findId(relativePath) != null) {
            throw new RepositoryException("Fajl vec postoji: " + relativePath);
        }

        int slash = relativePath.lastIndexOf('/');
        String parentPath = slash < 0 ? "" : relativePath.substring(0, slash);
        String name = relativePath.substring(slash + 1);

        String parentId = paths.findId(parentPath);
        if (parentId == null) {
            throw new RepositoryException("Folder ne postoji: " + parentPath);
        }

        File metadata = new File();
        metadata.setName(name);
        metadata.setParents(List.of(parentId));

        try {
            File created = drive.files()
                    .create(metadata, new InputStreamContent("application/octet-stream", content))
                    .setFields("id")
                    .execute();
            paths.remember(relativePath, created.getId());
        } catch (IOException e) {
            throw new RepositoryException("Greska pri upisu: " + relativePath, e);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        return opened().exists(relativePath);
    }

    @Override
    public List<String> listDirectories(String relativePath) {
        return opened().listDirectories(relativePath);
    }

    @Override
    public List<String> listFiles(String relativePath) {
        return opened().listFiles(relativePath);
    }

    @Override
    public InputStream read(String relativePath) {
        return opened().read(relativePath);
    }

    private String createFolder(String name, String parentId) {
        File metadata = new File();
        metadata.setName(name);
        metadata.setMimeType(DrivePaths.FOLDER_TYPE);
        metadata.setParents(List.of(parentId));

        try {
            return drive.files().create(metadata).setFields("id").execute().getId();
        } catch (IOException e) {
            throw new RepositoryException("Greska pri pravljenju foldera: " + name, e);
        }
    }

    private DriveRepositoryReader opened() {
        if (reader == null) {
            throw new RepositoryException("Repozitorijum nije otvoren, prvo pozovi createRepositoryAt ili openRepositoryAt");
        }
        return reader;
    }

    private DrivePaths paths() {
        return opened().getPaths();
    }
}
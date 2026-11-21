package resolver.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import repository.common.RepositoryException;
import repository.common.RepositoryReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// sve putanje se prevode u ID preko DrivePaths
public class DriveRepositoryReader implements RepositoryReader {

    private final Drive drive;
    private final DrivePaths paths;

    public DriveRepositoryReader(Drive drive, String rootId) {
        this.drive = drive;
        this.paths = new DrivePaths(drive, rootId);
    }

    // trebaju writer-u iz publisher-drive
    public Drive getDrive() {
        return drive;
    }

    public DrivePaths getPaths() {
        return paths;
    }

    @Override
    public boolean exists(String relativePath) {
        return paths.findId(relativePath) != null;
    }

    @Override
    public List<String> listDirectories(String relativePath) {
        return list(relativePath, true);
    }

    @Override
    public List<String> listFiles(String relativePath) {
        return list(relativePath, false);
    }

    @Override
    public InputStream read(String relativePath) {
        String id = paths.findId(relativePath);
        if (id == null) {
            throw new RepositoryException("Fajl ne postoji: " + relativePath);
        }

        try {
            return drive.files().get(id).executeMediaAsInputStream();
        } catch (IOException e) {
            throw new RepositoryException("Greska pri citanju: " + relativePath, e);
        }
    }

    private List<String> list(String relativePath, boolean folders) {
        List<String> result = new ArrayList<>();

        String id = paths.findId(relativePath);
        if (id == null) {
            return result; // prazna lista ako folder ne postoji, ovako smo kod lokala
        }

        for (File child : paths.listChildren(id)) {
            if (DrivePaths.isFolder(child) == folders) {
                result.add(child.getName());
            }
        }
        return result;
    }
}
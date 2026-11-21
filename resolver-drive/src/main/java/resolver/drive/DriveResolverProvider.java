package resolver.drive;

import com.google.api.services.drive.Drive;
import repository.common.RepositoryException;
import repository.common.RepositoryReader;
import resolver.api.ResolverProvider;

import java.util.Map;

// drajver za google drive, ucitava se preko META-INF/services
// location je ime foldera repozitorijuma u korenu drive-a
public class DriveResolverProvider implements ResolverProvider {

    @Override
    public String name() {
        return "gdrive";
    }

    @Override
    public RepositoryReader open(String location, Map<String, String> options) {
        // kredencijali se citaju iz config/credentials.json
        // da li ovo uospte treba ovde da stoji batonga????
        Drive drive = DriveConnection.connect();

        String rootId = DrivePaths.findRootFolder(drive, location);
        if (rootId == null) {
            throw new RepositoryException("Repozitorijum ne postoji na Drive-u: " + location);
        }

        return new DriveRepositoryReader(drive, rootId);
    }
}
package repository.publisher.drive;

import repository.common.RepositoryWriter;
import repository.publisher.PublisherProvider;

import java.util.Map;

public class DrivePublisherProvider implements PublisherProvider {

    @Override
    public String name() {
        return "gdrive";
    }

    @Override
    public RepositoryWriter newWriter(Map<String, String> options) {
        return new DriveRepositoryWriter();
    }
}
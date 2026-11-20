package publisher.raf;

import repository.common.RepositoryWriter;
import repository.publisher.PublisherProvider;

import java.util.Map;

public class RafPublisherProvider implements PublisherProvider {
    @Override
    public String name() {
        return "raf";
    }

    @Override
    public RepositoryWriter newWriter(Map<String, String> options) {
        return new RafRepositoryWriter("sftp.example.invalid", "REDACTED_USER", "REDACTED_PASSWORD");
    }
}

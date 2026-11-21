package repository.publisher.drive;

import dependency.resolver.DefaultDependencyResolver;
import repository.common.*;
import repository.publisher.Publisher;
import resolver.api.ConflictStrategy;
import resolver.api.ResolutionResult;
import resolver.api.ResolveOptions;
import repository.publisher.drive.DriveStorageProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DriveFullTest {

    private static final String REPO = "sk-test-2";

    public static void main(String[] args) throws Exception {
        Path artifacts = Files.createTempDirectory("sk-artefakti");

        Storage reader = new DriveStorageProvider().create(Map.of());
        Publisher publisher = new Publisher(reader);
        publisher.create(REPO, new RepositoryConfig(Set.of("jar"), Map.of("compile", true, "test", false)));
        System.out.println("Napravljen repozitorijum " + REPO);

        publish(publisher, artifacts, "rs.raf:x:1");
        publish(publisher, artifacts, "rs.raf:x:2");
        publish(publisher, artifacts, "rs.raf:opt:1");
        publish(publisher, artifacts, "rs.raf:t:1");
        publish(publisher, artifacts, "rs.raf:lib2:1", dep("rs.raf:x:2", "compile", false));
        publish(publisher, artifacts, "rs.raf:app:1",
                dep("rs.raf:x:1", "compile", false),
                dep("rs.raf:opt:1", "compile", true),   // optional, ne povlaci se
                dep("rs.raf:t:1", "test", false));      // test scope, ne povlaci se
        publish(publisher, artifacts, "rs.raf:other:1", dep("rs.raf:lib2:1", "compile", false));

        expectFail("vec postoji", () -> publish(publisher, artifacts, "rs.raf:x:1"));
        expectFail("nema zavisnosti", () -> publish(publisher, artifacts, "rs.raf:y:1", dep("rs.raf:nema:9", "compile", false)));
        expectFail("los scope", () -> publish(publisher, artifacts, "rs.raf:y:1", dep("rs.raf:x:1", "runtime", false)));

        for (ConflictStrategy strategy : ConflictStrategy.values()) {
            DefaultDependencyResolver resolver =
                    new DefaultDependencyResolver(new DriveStorageProvider().create(Map.of()));

            Path target = Files.createTempDirectory("sk-preuzeto-" + strategy);
            ResolutionResult result = resolver.resolve(
                    List.of(id("rs.raf:app:1"), id("rs.raf:other:1")),
                    new ResolveOptions(target, strategy, false));

            System.out.println(strategy + " -> " + result.downloaded().keySet());
            System.out.println("  preuzeto u " + target);
        }
    }


    private static void publish(Publisher publisher, Path dir, String id, Dependency... deps) {
        try {
            ComponentId componentId = id(id);
            Path artifact = dir.resolve(componentId.name() + "-" + componentId.version() + ".jar");
            if (!Files.exists(artifact)) {
                Files.writeString(artifact, "sadrzaj " + id);
            }
            publisher.publish(new Component(componentId, List.of(deps)), artifact);
            System.out.println("Objavljeno " + id);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void expectFail(String label, Runnable action) {
        try {
            action.run();
            System.out.println("GRESKA: nije odbijeno [" + label + "]");
        } catch (Exception e) {
            System.out.println("OK odbijeno [" + label + "]: " + e.getMessage());
        }
    }

    private static Dependency dep(String id, String scope, boolean optional) {
        return new Dependency(id(id), scope, optional);
    }

    private static ComponentId id(String text) {
        String[] p = text.split(":");
        return new ComponentId(p[0], p[1], p[2]);
    }
}
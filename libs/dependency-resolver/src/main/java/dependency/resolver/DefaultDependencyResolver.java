package dependency.resolver;

import repository.common.*;
import resolver.api.ComponentIds;
import resolver.api.ConflictStrategy;
import resolver.api.DependencyResolver;
import resolver.api.ResolutionResult;
import resolver.api.ResolveOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

// todo:
//  - da li sam lepo razumeo da, ako npr. u repozitorijumu stoji da je neki scope
//    optional, ali u komponenti stoji da ipak treba da se skine, da onda treba da
//    ga skinemo, i da li se to ovde desava?
//  - resolve da ne radi preko liste kompoennti nego samo komponentu po komponentu?
//    i onda mozda kasnije moze overload?
public class DefaultDependencyResolver implements DependencyResolver {

    private final RepositoryReader reader;

    public DefaultDependencyResolver(RepositoryReader reader) {
        this.reader = reader;
    }

    private static class FoundComponent {
        ComponentId id;
        int depth;

        public FoundComponent(ComponentId id, int depth) {
            this.id = id;
            this.depth = depth;
        }
    }

    private static String makeKey(ComponentId id) {
        return id.organization() + ":" + id.name();
    }

    @Override
    public ResolutionResult resolve(List<ComponentId> roots, ResolveOptions options) {

        RepositoryConfig config = readConfig();

        Map<String, String> forcedVersions = new HashMap<>();

        Map<String, ComponentId> selected;
        Map<String, List<String>> seenVersions;

        while (true) {
            seenVersions = new HashMap<>();
            selected = walk(roots, config, forcedVersions, seenVersions);

            // BFS prvo nalazi komponente blize korenu, pa je jedan prolaz dovoljan
            if (options.conflictStrategy() == ConflictStrategy.NEAREST_TO_ROOT) {
                break;
            }

            // ako negde vim noviju verziju, ponovo obilazimo graf
            boolean changed = false;

            for (String key : seenVersions.keySet()) {
                ComponentId chosen = selected.get(key);
                if (chosen == null) {
                    continue;
                }

                String newest = findNewest(seenVersions.get(key));

                if (compareVersions(newest, chosen.version()) > 0) {
                    forcedVersions.put(key, newest);
                    changed = true;
                }
            }

            if (!changed) {
                break;
            }
        }

        try {
            Files.createDirectories(options.targetDir());
        } catch (IOException e) {
            throw new RuntimeException("Error creating target directory", e);
        }

        Map<ComponentId, Path> downloaded = new LinkedHashMap<>();

        for (ComponentId id : selected.values()) {
            Path file = download(id, options.targetDir());
            downloaded.put(id, file);
        }

        return new ResolutionResult(downloaded);
    }

    private Map<String, ComponentId> walk(List<ComponentId> roots,
                                          RepositoryConfig config,
                                          Map<String, String> forcedVersions,
                                          Map<String, List<String>> seenVersions) {

        Map<String, ComponentId> selected = new LinkedHashMap<>();
        Queue<FoundComponent> queue = new LinkedList<>();

        for (ComponentId root : roots) {
            addToQueue(root, 0, forcedVersions, selected, seenVersions, queue);
        }

        while (!queue.isEmpty()) {

            FoundComponent current = queue.poll();

            Component componentData = readComponent(current.id);

            for (Dependency dependency : componentData.dependencies()) {

                // komponente iz fajla su direktne zavisnosti projekta,
                // pa su njihove zavisnosti vec tranzitivne i za njih vaze pravila
                if (dependency.optional()) {
                    continue;
                }

                Boolean transitive = config.scopes().get(dependency.scope());
                if (transitive == null || !transitive) {
                    continue;
                }

                addToQueue(dependency.id(), current.depth + 1,
                        forcedVersions, selected, seenVersions, queue);
            }
        }

        return selected;
    }

    private void addToQueue(ComponentId id, int depth,
                            Map<String, String> forcedVersions,
                            Map<String, ComponentId> selected,
                            Map<String, List<String>> seenVersions,
                            Queue<FoundComponent> queue) {

        String key = makeKey(id);

        if (!seenVersions.containsKey(key)) {
            seenVersions.put(key, new ArrayList<>());
        }
        seenVersions.get(key).add(id.version());

        if (selected.containsKey(key)) {
            return;
        }

        String version = id.version();
        if (forcedVersions.containsKey(key)) {
            version = forcedVersions.get(key);
        }

        ComponentId chosen = new ComponentId(id.organization(), id.name(), version);

        selected.put(key, chosen);
        queue.add(new FoundComponent(chosen, depth));
    }

    private String findNewest(List<String> versions) {
        String newest = versions.get(0);

        for (String version : versions) {
            if (compareVersions(version, newest) > 0) {
                newest = version;
            }
        }

        return newest;
    }

    private int compareVersions(String a, String b) {
        String[] partsA = a.split("[.\\-]");
        String[] partsB = b.split("[.\\-]");

        int length = Math.max(partsA.length, partsB.length);

        for (int i = 0; i < length; i++) {
            String partA = i < partsA.length ? partsA[i] : "0";
            String partB = i < partsB.length ? partsB[i] : "0";

            int result;

            if (isNumber(partA) && isNumber(partB)) {
                result = Long.compare(Long.parseLong(partA), Long.parseLong(partB));
            } else {
                result = partA.compareTo(partB);
            }

            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    private boolean isNumber(String s) {
        if (s.isEmpty()) {
            return false;
        }

        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }


    private RepositoryConfig readConfig() {
        String path = RepositoryLayout.configFile();
        if (!reader.exists(path)) {
            throw new RepositoryException("Repository config not found");
        }
        return Serializer.deserializeConfig(readText(path));
    }

    private Component readComponent(ComponentId id) {
        String path = RepositoryLayout.metadataFile(id);
        if (!reader.exists(path)) {
            throw new RepositoryException("Component not found: " + id);
        }
        List<Dependency> dependencies = Serializer.deserializeDependencies(readText(path));
        return new Component(id, dependencies == null ? List.of() : dependencies);
    }

    private Path download(ComponentId id, Path targetDir) {
        String fileName = findArtifactName(id);
        String source = RepositoryLayout.componentDir(id) + "/" + fileName;

        try (InputStream in = reader.read(source)) {
            Path target = targetDir.resolve(fileName);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new RepositoryException("Error downloading component: " + id, e);
        }
    }

    // fajl komponente je jedini fajl u folderu verzije koji nije fajl sa zavisnostima
    private String findArtifactName(ComponentId id) {
        String dir = RepositoryLayout.componentDir(id);
        if (!reader.exists(dir)) {
            throw new RepositoryException("Component not found: " + id);
        }
        for (String name : reader.listFiles(dir)) {
            if (!name.equals(RepositoryLayout.METADATA_FILE)) {
                return name;
            }
        }
        throw new RepositoryException("Component file not found: " + id);
    }

    private String readText(String path) {
        try (InputStream in = reader.read(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Error reading " + path, e);
        }
    }

    @Override
    public ResolutionResult resolveFromFile(Path componentsFile, ResolveOptions options) {

        List<ComponentId> roots = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(componentsFile);

            for (String line : lines) {
                line = line.trim();

                if (!line.isEmpty()) {
                    roots.add(ComponentIds.parse(line));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading components file", e);
        }

        return resolve(roots, options);
    }
}
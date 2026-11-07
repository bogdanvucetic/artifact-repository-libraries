package org.example.dependency.resolver;

import org.example.repository.common.Component;
import org.example.repository.common.ComponentId;
import org.example.repository.common.Dependency;
import org.example.repository.common.RepositoryConfig;
import org.example.resolver.api.ComponentIds;
import org.example.resolver.api.ConflictStrategy;
import org.example.resolver.api.DependencyResolver;
import org.example.resolver.api.RepositoryStorage;
import org.example.resolver.api.ResolutionResult;
import org.example.resolver.api.ResolveOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DefaultDependencyResolver implements DependencyResolver {

    private final RepositoryStorage storage;

    public DefaultDependencyResolver(RepositoryStorage storage) {
        this.storage = storage;
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

        RepositoryConfig config = storage.readConfig();

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
            Path file = storage.download(id, options.targetDir());
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

            Component componentData = storage.readComponent(current.id);

            boolean direct = current.depth == 0;

            for (Dependency dependency : componentData.dependencies()) {

                if (!direct) {
                    if (dependency.optional()) {
                        continue;
                    }

                    Boolean transitive = config.scopes().get(dependency.scope());
                    if (transitive == null || !transitive) {
                        continue;
                    }
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
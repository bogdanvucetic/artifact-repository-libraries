package repository.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RepositoryMetadata {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private RepositoryMetadata() {
    }

    public static void writeConfig(Path file, RepositoryConfig config) {
        JsonObject json = new JsonObject();

        JsonArray extensions = new JsonArray();
        for (String extension : config.allowedExtensions()) {
            extensions.add(extension);
        }
        json.add("allowedExtensions", extensions);

        JsonObject scopes = new JsonObject();
        for (String scope : config.scopes().keySet()) {
            scopes.addProperty(scope, config.scopes().get(scope));
        }
        json.add("scopes", scopes);

        writeJson(file, json);
    }

    public static RepositoryConfig readConfig(Path file) {
        JsonObject json = readJson(file);

        Set<String> extensions = new HashSet<>();
        for (JsonElement element : json.getAsJsonArray("allowedExtensions")) {
            extensions.add(element.getAsString());
        }

        Map<String, Boolean> scopes = new HashMap<>();
        JsonObject scopesJson = json.getAsJsonObject("scopes");
        for (String scope : scopesJson.keySet()) {
            scopes.put(scope, scopesJson.get(scope).getAsBoolean());
        }

        return new RepositoryConfig(extensions, scopes);
    }

    public static void writeDependencies(Path file, List<Dependency> dependencies) {
        JsonArray array = new JsonArray();

        for (Dependency dependency : dependencies) {
            JsonObject item = new JsonObject();
            item.addProperty("organization", dependency.id().organization());
            item.addProperty("name", dependency.id().name());
            item.addProperty("version", dependency.id().version());
            item.addProperty("scope", dependency.scope());
            item.addProperty("optional", dependency.optional());
            array.add(item);
        }

        JsonObject json = new JsonObject();
        json.add("dependencies", array);

        writeJson(file, json);
    }

    public static List<Dependency> readDependencies(Path file) {
        JsonObject json = readJson(file);

        List<Dependency> result = new ArrayList<>();

        for (JsonElement element : json.getAsJsonArray("dependencies")) {
            JsonObject item = element.getAsJsonObject();

            ComponentId id = new ComponentId(
                    item.get("organization").getAsString(),
                    item.get("name").getAsString(),
                    item.get("version").getAsString()
            );

            String scope = item.get("scope").getAsString();
            boolean optional = item.get("optional").getAsBoolean();

            result.add(new Dependency(id, scope, optional));
        }

        return result;
    }

    private static void writeJson(Path file, JsonObject json) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                gson.toJson(json, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing file: " + file, e);
        }
    }

    private static JsonObject readJson(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }
    }
}
package repository.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public final class Serializer {
    // todo: generalne provere validnosti podataka
    //  - da nisu null
    //  - da json-i imaju očekivanu strukturu
    //  - da deserijalizovani objekti poštuju ograničenja modela

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Serializer() { }

    public static String serializeConfig(RepositoryConfig config) {
        return gson.toJson(config);
    }

    // koristi LocalRepositoryStorage.readConfig()
    public static RepositoryConfig deserializeConfig(String json) {
        return gson.fromJson(json, RepositoryConfig.class);
    }
    /**
     * public static RepositoryConfig readConfig(Path file) {
     *         JsonObject json = readJson(file);
     *
     *         Set<String> extensions = new HashSet<>();
     *         for (JsonElement element : json.getAsJsonArray("allowedExtensions")) {
     *             extensions.add(element.getAsString());
     *         }
     *
     *         Map<String, Boolean> scopes = new HashMap<>();
     *         JsonObject scopesJson = json.getAsJsonObject("scopes");
     *         for (String scope : scopesJson.keySet()) {
     *             scopes.put(scope, scopesJson.get(scope).getAsBoolean());
     *         }
     *
     *         return new RepositoryConfig(extensions, scopes);
     *     }
     */

    public static String serializeDependencies(List<Dependency> dependencies) {
        return gson.toJson(dependencies);
    }

    // koristi LocalRepositoryStorage.readComponent()
    public static List<Dependency> deserializeDependencies (String json) {
        // Neophodno kako bi Gson mogao da uvaži podtip liste pri deserijalizaciji
        Type type = new TypeToken<List<Dependency>>() {}.getType();
        return gson.fromJson(json, type);
    }
    /**
     * public static List<Dependency> readDependencies(Path file) {
     *         JsonObject json = readJson(file);
     *
     *         List<Dependency> result = new ArrayList<>();
     *
     *         for (JsonElement element : json.getAsJsonArray("dependencies")) {
     *             JsonObject item = element.getAsJsonObject();
     *
     *             ComponentId id = new ComponentId(
     *                     item.get("organization").getAsString(),
     *                     item.get("name").getAsString(),
     *                     item.get("version").getAsString()
     *             );
     *
     *             String scope = item.get("scope").getAsString();
     *             boolean optional = item.get("optional").getAsBoolean();
     *
     *             result.add(new Dependency(id, scope, optional));
     *         }
     *
     *         return result;
     *     }
     */

    /**
     * private static JsonObject readJson(Path file) {
     *         try (Reader reader = Files.newBufferedReader(file)) {
     *             return gson.fromJson(reader, JsonObject.class);
     *         } catch (IOException e) {
     *             throw new RuntimeException("Error reading file: " + file, e);
     *         }
     *     }
     */
}

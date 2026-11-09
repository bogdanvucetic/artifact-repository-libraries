package org.example.repository.publisher;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.repository.common.Dependency;
import org.example.repository.common.RepositoryConfig;

import java.lang.reflect.Type;
import java.util.List;

public final class Serializer {
    // todo: generalne provere validnosti podataka
    //  - da nisu null
    //  - da json-i imaju očekivanu strukturu
    //  - da deserijalizovani objekti poštuju ograničenja modela

    private static final Gson gson = new Gson();

    private Serializer() { }

    public static String serializeConfig(RepositoryConfig config) {
        return gson.toJson(config);
    }

    public static RepositoryConfig deserializeConfig(String json) {
        return gson.fromJson(json, RepositoryConfig.class);
    }

    public static String serializeDependencies(List<Dependency> dependencies) {
        return gson.toJson(dependencies);
    }

    public static List<Dependency> deserializeDependencies (String json) {
        // Neophodno kako bi Gson mogao da uvaži podtip liste pri deserijalizaciji
        Type type = new TypeToken<List<Dependency>>() {}.getType();
        return gson.fromJson(json, type);
    }
}

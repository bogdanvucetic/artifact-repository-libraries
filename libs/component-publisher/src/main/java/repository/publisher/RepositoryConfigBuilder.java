package repository.publisher;

import repository.common.RepositoryConfig;

import java.util.*;

public class RepositoryConfigBuilder {
    private final Set<String> allowedExtensions = new HashSet<>();
    private final Map<String, Boolean> scopes = new HashMap<>();

    public RepositoryConfigBuilder addExtension(String extension) {
        Objects.requireNonNull(extension, "Ekstenzija ne sme biti null");

        if (extension.isBlank() || extension.startsWith("."))
            throw new IllegalArgumentException("Nevalidna ekstenzija: " + extension);

        allowedExtensions.add(extension);
        return this;
    }

    public RepositoryConfigBuilder addExtensions(List<String> extensions) {
        Objects.requireNonNull(extensions, "Ekstenzije ne smeju biti null");

        for (String extension : extensions)
            addExtension(extension);

        return this;

    }

    public RepositoryConfigBuilder addScope(String scope, Boolean transitive) {
        Objects.requireNonNull(scope, "Scope ne sme biti null");
        Objects.requireNonNull(transitive, "Transitive ne sme biti null");

        if (scope.isBlank())
            throw new IllegalArgumentException("Scope ne sme biti prazan");

        scopes.put(scope, transitive);
        return this;
    }

    public RepositoryConfigBuilder addScopes(Map<String, Boolean> scopes) {
        Objects.requireNonNull(scopes, "Scope-ovi ne smeju biti null");

        for (Map.Entry<String, Boolean> entry : scopes.entrySet())
            addScope(entry.getKey(), entry.getValue());

        return this;
    }

    public RepositoryConfig build() {
        return new RepositoryConfig(allowedExtensions, scopes);
    }
}

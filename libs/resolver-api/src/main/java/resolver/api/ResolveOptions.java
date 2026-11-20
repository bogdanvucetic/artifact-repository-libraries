package resolver.api;

import java.nio.file.Path;

public record ResolveOptions(
        Path targetDir,
        ConflictStrategy conflictStrategy,
        boolean includeOptionalRoots
) {
    public static ResolveOptions of(Path targetDir) {
        return new ResolveOptions(targetDir, ConflictStrategy.NEWEST_VERSION, true);
    }
}
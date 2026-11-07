public record Dependency(
        ComponentId id,
        String scope,
        boolean optional
) {
}

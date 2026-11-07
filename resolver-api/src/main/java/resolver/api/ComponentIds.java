package resolver.api;

import repository.common.ComponentId;

public final class ComponentIds {
    private ComponentIds() {}

    public static ComponentId parse(String text) {
        String[] p = text.trim().split(":");
        if (p.length != 3) throw new IllegalArgumentException("Ocekivano organizacija:naziv:verzija, a stiglo: " + text);
        return new ComponentId(p[0], p[1], p[2]);
    }

    public static ComponentId of(String organization, String name, String version) {
        return new ComponentId(organization, name, version);
    }
}
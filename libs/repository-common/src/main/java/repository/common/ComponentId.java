package repository.common;

import java.util.Objects;

public record ComponentId(
        String organization,
        String name,
        String version
) {
    public ComponentId {
        Objects.requireNonNull(organization, "Organizacija ne sme biti null");
        Objects.requireNonNull(name, "Naziv ne sme biti null");
        Objects.requireNonNull(version, "Verzija ne sme biti null");

        if (organization.isBlank())
            throw new IllegalArgumentException("Nevalidna organizacija: naziv organizacije ne sme biti prazan");

        if (name.isBlank())
            throw new IllegalArgumentException("Nevalidan projekat: naziv projekta ne sme biti prazan");

        if (version.isBlank())
            throw new IllegalArgumentException("Nevalidna verzija: verzija ne sme biti prazna");

        if (!organization.matches("\\w[\\w.-]*"))
            throw new IllegalArgumentException("Nevalidan naziv organizacije");

        if (!name.matches("\\w[\\w.-]*"))
            throw new IllegalArgumentException("Nevalidan naziv projekta");

        if (!version.matches("\\w[\\w.-]*"))
            throw new IllegalArgumentException("Nevalidna verzija");
    }
}

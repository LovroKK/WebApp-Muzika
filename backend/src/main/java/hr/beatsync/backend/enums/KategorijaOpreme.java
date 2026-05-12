package hr.beatsync.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum KategorijaOpreme {
    Mixeri("Mixeri"),
    Playeri("Playeri"),
    Rasvjeta("Rasvjeta"),
    Zvucnici("Zvučnici"),
    Mikrofoni("Mikrofoni"),
    Kontroleri("Kontroleri");

    private final String displayName;

    KategorijaOpreme(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static KategorijaOpreme fromValue(String value) {
        for (KategorijaOpreme kategorija : values()) {
            if (kategorija.name().equalsIgnoreCase(value)
                    || kategorija.displayName.equalsIgnoreCase(value)) {
                return kategorija;
            }
        }

        throw new IllegalArgumentException("Nepoznata kategorija opreme: " + value);
    }
}
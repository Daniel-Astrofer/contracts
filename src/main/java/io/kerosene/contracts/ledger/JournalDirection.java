package io.kerosene.contracts.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Journal entry direction: debit or credit.
 */
public enum JournalDirection {
    @JsonProperty("debit")
    DEBIT("debit"),
    @JsonProperty("credit")
    CREDIT("credit");

    private final String value;

    JournalDirection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static JournalDirection fromString(String direction) {
        if (direction == null) return null;
        for (var d : values()) {
            if (d.value.equalsIgnoreCase(direction)) return d;
        }
        throw new IllegalArgumentException("Unknown direction: " + direction);
    }
}

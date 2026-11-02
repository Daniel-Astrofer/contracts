package io.kerosene.contracts.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Journal entry direction: debit or credit.
 */
public enum JournalDirection {
    /** Reduces the balance of the account being posted to. */
    @JsonProperty("debit")
    DEBIT("debit"),
    /** Increases the balance of the account being posted to. */
    @JsonProperty("credit")
    CREDIT("credit");

    /** Stable lowercase JSON representation for this posting direction. */
    private final String value;

    /** @param value canonical wire value associated with the enum member */
    JournalDirection(String value) {
        this.value = value;
    }

    /** @return canonical lowercase JSON value for this direction */
    public String getValue() {
        return value;
    }

    /**
     * Resolves a serialized direction without changing the caller's input. Matching is
     * case-insensitive; null remains null so optional filters can represent no direction.
     *
     * @param direction wire or user input to resolve
     * @return matching enum value, or null for null input
     * @throws IllegalArgumentException if a non-null value is not {@code debit} or {@code credit}
     */
    public static JournalDirection fromString(String direction) {
        if (direction == null) return null;
        for (var d : values()) {
            if (d.value.equalsIgnoreCase(direction)) return d;
        }
        throw new IllegalArgumentException("Unknown direction: " + direction);
    }
}

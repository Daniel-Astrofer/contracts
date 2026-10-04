package com.kerosene.common.financial.model;

/**
 * Non-empty identifier for a Vault Mesh day epoch.
 * @param value opaque day epoch token supplied by the mesh protocol
 */
public record DayEpoch(String value) {
    /** Requires a non-blank epoch token.
     * @param value protocol token to validate
     * @throws IllegalArgumentException if the token is null or blank
     */
    public DayEpoch {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("day epoch required");
    }

    @Override
    /** Returns the protocol token unchanged.
     * @return epoch token
     */
    public String toString() { return value; }
}

package com.kerosene.common.financial.model;

/**
 * Non-empty machine-readable reason associated with a Vault Mesh settlement status.
 * @param value opaque reason token defined by the vault protocol
 */
public record ReasonCode(String value) {
    /** Requires a non-blank reason token.
     * @param value reason token to validate
     * @throws IllegalArgumentException if the value is null or blank
     */
    public ReasonCode {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("reason code required");
    }

    @Override
    /** Returns the reason token unchanged.
     * @return protocol reason token
     */
    public String toString() { return value; }
}

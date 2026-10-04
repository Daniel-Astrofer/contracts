package com.kerosene.common.financial.model;

/**
 * Non-empty identifier for an external financial or custody provider instance.
 * @param value provider name or configured instance identifier
 */
public record ProviderId(String value) {
    /** Requires a non-blank provider identifier.
     * @param value identifier to validate
     * @throws IllegalArgumentException if the identifier is null or blank
     */
    public ProviderId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("provider id required");
    }

    @Override
    /** Returns the provider identifier unchanged.
     * @return provider identifier
     */
    public String toString() { return value; }
}

package com.kerosene.common.financial.model;

import java.util.Set;

/**
 * Canonical settlement rail identifier accepted by the financial contracts.
 * Values are normalized to uppercase and restricted to on-chain Bitcoin, Lightning, or internal ledger.
 * @param value rail identifier; case-insensitive on input
 */
public record Rail(String value) {
    private static final Set<String> VALID = Set.of("BITCOIN_ONCHAIN", "LIGHTNING", "INTERNAL");

    /** Normalizes case and rejects values outside the supported rails.
     * @param value candidate rail name
     * @throws IllegalArgumentException if the value is blank or unsupported
     */
    public Rail {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("rail value required");
        String upper = value.toUpperCase();
        if (!VALID.contains(upper)) throw new IllegalArgumentException("unknown rail: " + value);
        value = upper;
    }

    @Override
    /** Returns the normalized rail name.
     * @return canonical uppercase rail identifier
     */
    public String toString() { return value; }
}

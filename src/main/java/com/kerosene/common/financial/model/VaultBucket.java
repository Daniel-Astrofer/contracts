package com.kerosene.common.financial.model;

import java.util.Set;

/**
 * Canonical custody bucket name recognized by the Vault Mesh protocol.
 * Input is case-insensitive and stored in uppercase.
 * @param value bucket identifier; currently {@code USERS}, {@code CHANNELS}, or {@code RESERVES}
 */
public record VaultBucket(String value) {
    private static final Set<String> VALID = Set.of("USERS", "CHANNELS", "RESERVES");

    /** Normalizes the identifier and rejects buckets not supported by this protocol version.
     * @param value candidate bucket name
     * @throws IllegalArgumentException if the value is blank or unknown
     */
    public VaultBucket {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("bucket value required");
        String upper = value.toUpperCase();
        if (!VALID.contains(upper)) throw new IllegalArgumentException("unknown vault bucket: " + value);
        value = upper;
    }

    @Override
    /** Returns the canonical uppercase bucket name.
     * @return custody bucket identifier
     */
    public String toString() { return value; }
}

package com.kerosene.common.financial.model;

/**
 * Opaque identifier digest for the governance constitution used by Vault Mesh.
 * @param value constitution digest accepted by the protocol; at least 32 characters
 */
public record ConstitutionHash(String value) {
    /** Rejects absent or undersized constitution digests.
     * @param value candidate digest
     * @throws IllegalArgumentException if null, blank, or shorter than 32 characters
     */
    public ConstitutionHash {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("constitution hash required");
        if (value.length() < 32) throw new IllegalArgumentException("constitution hash too short: " + value.length());
    }

    @Override
    /** Returns the digest in its original representation.
     * @return constitution digest
     */
    public String toString() { return value; }
}

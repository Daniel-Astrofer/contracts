package com.kerosene.common.financial.model;

/**
 * Opaque identifier digest used to bind a spend to a Vault Mesh policy.
 * @param value policy digest accepted by the protocol; at least 32 characters
 */
public record PolicyHash(String value) {
    /** Rejects absent or undersized policy digests.
     * @param value candidate digest
     * @throws IllegalArgumentException if null, blank, or shorter than 32 characters
     */
    public PolicyHash {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("policy hash required");
        if (value.length() < 32) throw new IllegalArgumentException("policy hash too short: " + value.length());
    }

    @Override
    /** Returns the digest in its original representation.
     * @return policy digest
     */
    public String toString() { return value; }
}

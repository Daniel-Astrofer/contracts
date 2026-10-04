package com.kerosene.common.financial.model;

/**
 * Canonical hexadecimal Lightning payment hash.
 * The value is normalized to lowercase and contains 32 bytes encoded as 64 hex characters.
 * @param value hexadecimal SHA-256 payment hash
 */
public record PaymentHash(String value) {
    private static final int HEX_LENGTH = 64;

    /** Validates the fixed hash length and hexadecimal alphabet, then normalizes case.
     * @param value candidate payment hash
     * @throws IllegalArgumentException if absent or not exactly 32-byte hexadecimal
     */
    public PaymentHash {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("payment hash required");
        if (value.length() != HEX_LENGTH) throw new IllegalArgumentException("payment hash must be " + HEX_LENGTH + " hex chars, got: " + value.length());
        if (!value.matches("[0-9a-fA-F]+")) throw new IllegalArgumentException("payment hash must be hex");
        value = value.toLowerCase();
    }

    @Override
    /** Returns the normalized payment hash.
     * @return lowercase 64-character hexadecimal hash
     */
    public String toString() { return value; }
}

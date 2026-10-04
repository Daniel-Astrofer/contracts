package com.kerosene.common.financial.model;

/**
 * 32-byte x-only public key (64 hex chars) for Taproot outputs.
 * The value is normalized to lowercase hexadecimal.
 * @param value 64-character hex-encoded x coordinate of the Taproot public key
 */
public record XOnlyPublicKey(String value) {
    private static final int HEX_LENGTH = 64;

    /** Validates the encoded key length and alphabet, then canonicalizes letter case.
     * @param value hex-encoded x-only public key
     * @throws IllegalArgumentException if the value is absent or not exactly 32-byte hex
     */
    public XOnlyPublicKey {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("x-only pubkey required");
        if (value.length() != HEX_LENGTH) throw new IllegalArgumentException("x-only pubkey must be " + HEX_LENGTH + " hex chars, got: " + value.length());
        if (!value.matches("[0-9a-fA-F]+")) throw new IllegalArgumentException("x-only pubkey must be hex");
        value = value.toLowerCase();
    }

    @Override
    /** Returns the normalized lowercase public key.
     * @return 64-character lowercase hexadecimal key
     */
    public String toString() { return value; }
}

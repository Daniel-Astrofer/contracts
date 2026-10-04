package com.kerosene.common.financial.model;

import java.util.Arrays;

/**
 * Immutable cryptographic nonce for challenge and replay-protection protocols.
 * Input is defensively copied and limited to 12–32 bytes.
 * @param value nonce bytes; the stored component is a defensive copy
 */
public record Nonce(byte[] value) {
    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 32;

    /** Validates nonce length and stores a defensive copy of the supplied bytes.
     * @param value nonce bytes to validate
     * @throws IllegalArgumentException if null, empty, shorter than 12 bytes, or longer than 32 bytes
     */
    public Nonce {
        if (value == null || value.length == 0) throw new IllegalArgumentException("nonce required");
        if (value.length < MIN_LENGTH) throw new IllegalArgumentException("nonce too short: " + value.length + " (min " + MIN_LENGTH + ")");
        if (value.length > MAX_LENGTH) throw new IllegalArgumentException("nonce too long: " + value.length + " (max " + MAX_LENGTH + ")");
        value = value.clone();
    }

    /** Returns a defensive copy so callers cannot mutate the nonce held by this value object.
     * @return copied nonce bytes
     */
    public byte[] value() { return value.clone(); }

    @Override
    /** Returns a redacted diagnostic representation that never exposes nonce bytes.
     * @return nonce marker containing only its byte length
     */
    public String toString() { return "Nonce[len=" + value.length + "]"; }

    @Override
    /** Compares nonce contents without exposing or converting the bytes.
     * @param o object to compare
     * @return {@code true} when the other value is a nonce with identical bytes
     */
    public boolean equals(Object o) {
        return o instanceof Nonce other && Arrays.equals(value, other.value);
    }

    @Override
    /** Returns the content-based hash required by the value-object equality contract.
     * @return hash derived from nonce bytes
     */
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}

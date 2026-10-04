package com.kerosene.common.financial.model;

/**
 * Base64 text carrying a partially signed Bitcoin transaction.
 * This value object checks the encoded alphabet; transaction decoding and PSBT validity
 * are checked by the consuming Bitcoin implementation.
 * @param value Base64-encoded PSBT text
 */
public record PsbtBase64(String value) {
    /** Requires non-empty Base64-alphabet text.
     * @param value encoded PSBT to validate
     * @throws IllegalArgumentException if the input is blank or contains non-Base64 characters
     */
    public PsbtBase64 {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("psbt required");
        if (!value.matches("^[A-Za-z0-9+/=]+$")) throw new IllegalArgumentException("psbt must be valid base64");
    }

    @Override
    /** Returns the encoded PSBT text unchanged.
     * @return Base64 PSBT
     */
    public String toString() { return value; }
}

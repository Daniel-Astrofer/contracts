package com.kerosene.common.financial.model;

/**
 * Canonical hash identifying a Vault Mesh governance proposal.
 * It is represented as 32 bytes of hexadecimal text and normalized to lowercase.
 * @param value hexadecimal digest of the canonical proposal
 */
public record ProposalHash(String value) {
    private static final int HEX_LENGTH = 64;

    /** Validates a 32-byte hexadecimal digest and normalizes its case.
     * @param value candidate proposal digest
     * @throws IllegalArgumentException if absent or not exactly 32-byte hexadecimal
     */
    public ProposalHash {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("proposal hash required");
        if (value.length() != HEX_LENGTH) throw new IllegalArgumentException("proposal hash must be " + HEX_LENGTH + " hex chars, got: " + value.length());
        if (!value.matches("[0-9a-fA-F]+")) throw new IllegalArgumentException("proposal hash must be hex");
        value = value.toLowerCase();
    }

    @Override
    /** Returns the normalized proposal digest.
     * @return lowercase 64-character hexadecimal digest
     */
    public String toString() { return value; }
}

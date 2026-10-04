package com.kerosene.common.financial.model;

import java.util.regex.Pattern;

/**
 * Bitcoin address accepted by the on-chain transfer contract (legacy, SegWit, or Taproot form).
 * Validation checks the lexical network prefix and alphabet; full checksum/script validity is
 * delegated to the Bitcoin adapter.
 * @param value address text preserved for the wire representation
 */
public record BitcoinAddress(String value) {
    private static final Pattern BECH32 = Pattern.compile("^(bc|tb|bcrt)1[ac-hj-np-z02-9]{6,}$");
    private static final Pattern BECH32M = Pattern.compile("^(bc|tb|bcrt)1p[ac-hj-np-z02-9]{6,}$");
    private static final Pattern BASE58 = Pattern.compile("^[13mn][1-9A-HJ-NP-Za-km-z]{25,34}$");

    /** Rejects absent text and values outside the supported address formats.
     * @param value address text to validate
     * @throws IllegalArgumentException when the value is blank or malformed
     */
    public BitcoinAddress {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("address required");
        if (!BECH32.matcher(value).matches()
                && !BECH32M.matcher(value).matches()
                && !BASE58.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid bitcoin address format: " + value.substring(0, Math.min(value.length(), 10)) + "...");
        }
    }

    @Override
    /** Returns the address in its original validated representation.
     * @return address text
     */
    public String toString() { return value; }
}

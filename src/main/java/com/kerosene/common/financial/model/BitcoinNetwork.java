package com.kerosene.common.financial.model;

import java.util.Set;

/**
 * Canonical Bitcoin network identifier recognized by the platform.
 * Input is case-insensitive and stored in uppercase.
 * @param value one of {@code MAINNET}, {@code TESTNET}, {@code REGTEST}, or {@code SIGNET}
 */
public record BitcoinNetwork(String value) {
    /** Canonical network identifiers accepted by this contract version. */
    private static final Set<String> VALID = Set.of("MAINNET", "TESTNET", "REGTEST", "SIGNET");

    /** Normalizes the network name and rejects unknown networks.
     * @param value candidate network name
     * @throws IllegalArgumentException if blank or unsupported
     */
    public BitcoinNetwork {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("network value required");
        String upper = value.toUpperCase();
        if (!VALID.contains(upper)) throw new IllegalArgumentException("unknown bitcoin network: " + value);
        value = upper;
    }

    /** Returns the canonical uppercase network name.
     * @return Bitcoin network identifier
     */
    @Override
    public String toString() { return value; }
}

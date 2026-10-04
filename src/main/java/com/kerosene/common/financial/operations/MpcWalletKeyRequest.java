package com.kerosene.common.financial.operations;

import java.util.UUID;

/**
 * Parameters for provisioning a wallet-specific MPC signing key.
 *
 * @param walletId wallet receiving the provisioned key
 * @param userId owner associated with the wallet
 * @param scheme key-generation protocol such as FROST or DKG
 * @param network chain network for derived addresses
 * @param purpose intended use such as inbound, outbound, or change
 */
public record MpcWalletKeyRequest(
    UUID walletId,
    Long userId,
    String scheme,         // FROST, DKG, etc.
    String network,        // MAINNET, TESTNET
    String purpose         // INBOUND, OUTBOUND, CHANGE
) {
    /** Ensures every key-generation context field is present. */
    public MpcWalletKeyRequest {
        if (walletId == null) throw new IllegalArgumentException("walletId required");
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (scheme == null || scheme.isBlank()) throw new IllegalArgumentException("scheme required");
        if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
        if (purpose == null || purpose.isBlank()) throw new IllegalArgumentException("purpose required");
    }
}

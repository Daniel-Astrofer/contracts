package com.kerosene.common.financial.operations;

import java.time.Instant;
import java.util.UUID;

/**
 * Public result and cryptographic evidence for an MPC-provisioned wallet key.
 *
 * @param keyId unique identifier assigned to the key by the MPC system
 * @param walletId wallet for which the key was provisioned
 * @param scheme key-generation scheme used
 * @param network network encoded into its derivation metadata
 * @param xOnlyPublicKey Taproot x-only public key in 64-character hex form
 * @param descriptor Bitcoin output descriptor for address derivation
 * @param address convenience address derived for the wallet
 * @param constitutionHash governance constitution digest authorizing provisioning
 * @param createdAt key creation timestamp
 * @param attestation protocol evidence authenticating the generated key metadata
 */
public record MpcWalletKeyReceipt(
    String keyId,                  // unique key identifier
    UUID walletId,
    String scheme,                 // FROST, DKG, etc.
    String network,                // MAINNET, TESTNET
    String xOnlyPublicKey,         // 64 hex chars — Taproot x-only pubkey
    String descriptor,             // Bitcoin output descriptor
    String address,                // derived Bitcoin address (for convenience)
    String constitutionHash,       // constitution that authorized this key
    Instant createdAt,             // when the key was provisioned
    String attestation             // cryptographic attestation from the MPC protocol
) {
    /** Requires all identity, derivation, governance, and attestation fields. */
    public MpcWalletKeyReceipt {
        if (keyId == null || keyId.isBlank()) throw new IllegalArgumentException("keyId required");
        if (walletId == null) throw new IllegalArgumentException("walletId required");
        if (scheme == null || scheme.isBlank()) throw new IllegalArgumentException("scheme required");
        if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
        if (xOnlyPublicKey == null || xOnlyPublicKey.isBlank()) throw new IllegalArgumentException("xOnlyPublicKey required");
        if (descriptor == null || descriptor.isBlank()) throw new IllegalArgumentException("descriptor required");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("address required");
        if (constitutionHash == null || constitutionHash.isBlank()) throw new IllegalArgumentException("constitutionHash required");
        if (createdAt == null) throw new IllegalArgumentException("createdAt required");
        if (attestation == null || attestation.isBlank()) throw new IllegalArgumentException("attestation required");
    }
}

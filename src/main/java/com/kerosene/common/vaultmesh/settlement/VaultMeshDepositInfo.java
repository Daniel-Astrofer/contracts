package com.kerosene.common.vaultmesh.settlement;

/**
 * Response model for {@code GET /v1/bitcoin/deposit}.
 *
 * <p>Shared Taproot deposit derived from the mesh Taproot group verifying key.
 * The descriptor may be omitted when only the address/key material is exposed.
 * @param address network-specific deposit address to monitor
 * @param descriptor output descriptor describing the shared Taproot output, when available
 * @param scheme cryptographic derivation scheme used by the vault mesh
 * @param outputPubkeyHex serialized output public key in hexadecimal
 * @param xonlyPubkeyHex x-only Taproot key in hexadecimal, when supplied
 * @param network Bitcoin network for which the address was derived
 */
public record VaultMeshDepositInfo(
        String address,
        String descriptor,
        String scheme,
        String outputPubkeyHex,
        String xonlyPubkeyHex,
        String network) {

    /** Requires the address, scheme, public key, and network metadata.
     * @param address deposit address
     * @param descriptor output descriptor, optionally null
     * @param scheme derivation scheme identifier
     * @param outputPubkeyHex output public key
     * @param xonlyPubkeyHex optional x-only key
     * @param network network identifier
     * @throws IllegalArgumentException if a required value is absent or optional key is blank
     */
    public VaultMeshDepositInfo {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("address required");
        if (scheme == null || scheme.isBlank()) throw new IllegalArgumentException("scheme required");
        if (outputPubkeyHex == null || outputPubkeyHex.isBlank()) throw new IllegalArgumentException("outputPubkeyHex required");
        if (xonlyPubkeyHex != null && xonlyPubkeyHex.isBlank()) {
            throw new IllegalArgumentException("xonlyPubkeyHex must be null or non-blank");
        }
        if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
    }
}

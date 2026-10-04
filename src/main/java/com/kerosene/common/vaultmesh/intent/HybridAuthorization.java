package com.kerosene.common.vaultmesh.intent;

/**
 * Dual Ed25519 + ML-DSA-65 (post-quantum) authorization.
 * Both signatures are REQUIRED for mainnet. Testnet may relax to single sig.
 *
 * @param ed25519SignatureHex Ed25519 signature over the canonical authorization payload
 * @param mlDsa65SignatureHex ML-DSA-65 signature; testnet-relaxed mode still carries the field
 * @param ed25519KeyId roster identifier for the Ed25519 verification key
 * @param mlDsaKeyId roster identifier for the ML-DSA-65 verification key
 * @param testnetRelaxed whether the explicitly relaxed testnet verification policy applies
 */
public record HybridAuthorization(
    String ed25519SignatureHex,
    String mlDsa65SignatureHex,
    String ed25519KeyId,
    String mlDsaKeyId,
    boolean testnetRelaxed
) {
    /** Ensures both key identifiers and signature fields are present in the envelope. */
    public HybridAuthorization {
        if (ed25519SignatureHex == null || ed25519SignatureHex.isBlank())
            throw new IllegalArgumentException("ed25519Signature required");
        if (mlDsa65SignatureHex == null || mlDsa65SignatureHex.isBlank())
            throw new IllegalArgumentException("mlDsa65Signature required (use empty string for testnet)");
        if (ed25519KeyId == null || ed25519KeyId.isBlank())
            throw new IllegalArgumentException("ed25519KeyId required");
        if (mlDsaKeyId == null || mlDsaKeyId.isBlank())
            throw new IllegalArgumentException("mlDsaKeyId required");
    }
}

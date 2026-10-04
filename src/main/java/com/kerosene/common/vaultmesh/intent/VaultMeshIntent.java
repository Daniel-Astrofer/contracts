package com.kerosene.common.vaultmesh.intent;

import java.time.Instant;

/**
 * Settlement intent model between KFE ledger and vault mesh custody plane.
 *
 * <p>Fire-and-forget from the bank: no FROST shares on the JVM. The intent carries
 * the settlement destination, amount, policy hash, and optional hybrid (Ed25519 + ML-DSA-65)
 * signatures for PQ-safe authorization. The vault mesh validates signatures (AND logic)
 * before executing.</p>
 *
 * @param intentId unique identifier used for idempotency and audit correlation
 * @param bucket custody-policy bucket against which the spend is authorized
 * @param destination recipient address or destination identifier
 * @param amountSats positive settlement amount in satoshis
 * @param policyHash digest binding the request to the approved policy
 * @param createdAt creation time used for validity and audit checks
 * @param ed25519SignatureHex Ed25519 signature over the canonical intent hash
 * @param mlDsa65SignatureHex ML-DSA-65 signature over the same canonical intent hash
 * @param ed25519KeyId roster identifier of the Ed25519 verification key
 * @param mlDsaKeyId roster identifier of the ML-DSA-65 verification key
 *
 * @since 0.1.0
 */
public record VaultMeshIntent(
        String intentId,
        String bucket,
        String destination,
        long amountSats,
        String policyHash,
        Instant createdAt,
        /** Ed25519 raw signature over canonical intent hash (hex-encoded). */
        String ed25519SignatureHex,
        /** ML-DSA-65 raw signature over canonical intent hash (hex-encoded). */
        String mlDsa65SignatureHex,
        /** Key identifier for the Ed25519 verification key (roster index). */
        String ed25519KeyId,
        /** Key identifier for the ML-DSA-65 verification key (roster index). */
        String mlDsaKeyId
) {
    /** Validates required identity and policy fields and requires a positive amount. */
    public VaultMeshIntent {
        if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
        if (bucket == null || bucket.isBlank()) throw new IllegalArgumentException("bucket required");
        if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0, got: " + amountSats);
        if (createdAt == null) throw new IllegalArgumentException("createdAt required");
    }
}

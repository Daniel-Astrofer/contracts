package com.kerosene.common.vaultmesh.intent;

import java.time.Instant;

/**
 * Settlement receipt with status, txid, and metadata from vault mesh.
 *
 * <p>Produced by the vault mesh after processing a {@link VaultMeshIntent}.
 * Status indicates acceptance, rejection, or fail-stop. On acceptance,
 * {@code txidOrProof} carries the Bitcoin transaction id.</p>
 * @param intentId identifier of the settlement intent this receipt answers
 * @param status accepted, rejected, or fail-stop settlement result
 * @param reasonCode machine-readable reason for rejection or fail-stop, when applicable
 * @param txidOrProof transaction ID on acceptance or protocol proof for other outcomes
 * @param completedAt time the mesh completed processing the intent
 *
 * @since 0.1.0
 */
public record VaultMeshReceipt(
        String intentId,
        Status status,
        String reasonCode,
        String txidOrProof,
        Instant completedAt
) {
    /** Stable category describing the mesh's terminal intent-processing outcome. */
    public enum Status {
        /** Mesh accepted the intent; transaction or settlement proof is available. */
        ACCEPTED,
        /** Mesh rejected the intent under policy or request validation. */
        REJECTED,
        /** Mesh entered fail-stop and requires operator or recovery action. */
        FAIL_STOP
    }

    /** Requires a correlated intent, status, and completion time.
     * @param intentId intent identifier
     * @param status settlement outcome
     * @param reasonCode optional protocol reason code
     * @param txidOrProof transaction ID or outcome proof
     * @param completedAt completion timestamp
     * @throws IllegalArgumentException if intentId, status, or completedAt is absent
     */
    public VaultMeshReceipt {
        if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
        if (status == null) throw new IllegalArgumentException("status required");
        if (completedAt == null) throw new IllegalArgumentException("completedAt required");
    }
}

package com.kerosene.common.vaultmesh.settlement;

import com.kerosene.common.vaultmesh.intent.VaultMeshReceipt;
import java.time.Instant;

/**
 * Result of vault-mesh PSBT signing (signed PSBT + policy receipt status).
 * Fields carrying a signed PSBT or signature proof are sensitive transaction artifacts.
 * @param intentId identifier of the spend intent authorized by the signing workflow
 * @param status accepted, rejected, or fail-stop receipt state
 * @param reasonCode machine-readable policy or execution reason, when applicable
 * @param signedPsbt signed PSBT returned for broadcast or further coordination
 * @param signatureProof protocol proof supporting the receipt decision
 * @param completedAt time at which the signing workflow reached this result
 */
public record VaultMeshPsbtReceipt(
        String intentId,
        VaultMeshReceipt.Status status,
        String reasonCode,
        String signedPsbt,
        String signatureProof,
        Instant completedAt
) {
    /** Requires an intent identifier, status, and completion timestamp.
     * @param intentId intent identifier
     * @param status receipt outcome
     * @param reasonCode optional reason discriminator
     * @param signedPsbt signed transaction artifact, if produced
     * @param signatureProof proof returned by the vault mesh
     * @param completedAt completion time
     * @throws IllegalArgumentException if intentId, status, or completedAt is absent
     */
    public VaultMeshPsbtReceipt {
        if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
        if (status == null) throw new IllegalArgumentException("status required");
        if (completedAt == null) throw new IllegalArgumentException("completedAt required");
    }
}

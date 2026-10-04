package com.kerosene.common.vaultmesh.settlement;

import java.time.Instant;

/**
 * Sealed hierarchy for PSBT signing results.
 * ACCEPTED must carry all verification fields.
 * REJECTED must explain why.
 * FAIL_STOP must indicate catastrophic halt.
 */
/** Typed outcome of vault-side PSBT signing and policy verification. */
public sealed interface VaultMeshPsbtResult
    permits VaultMeshPsbtResult.AcceptedPsbt,
            VaultMeshPsbtResult.RejectedPsbt,
            VaultMeshPsbtResult.FailStopPsbt {

    /** Accepted signed transaction with the evidence needed to independently verify it.
     * @param intentId authorized intent identifier
     * @param proposalHash digest of the proposal approved for signing
     * @param psbtHash digest of the PSBT that was verified
     * @param signedPsbtBase64 signed PSBT artifact
     * @param constitutionHash governing constitution digest
     * @param constitutionEpoch governance revision used for authorization
     * @param participantIds signer/member identifiers contributing to the quorum
     * @param threshold minimum signing threshold that was satisfied
     * @param transcriptHash digest of the signing transcript
     * @param aggregateProof cryptographic aggregate signature/proof
     * @param signerKeyId identifier of the key used to create the aggregate proof
     * @param network Bitcoin network against which the transaction was validated
     * @param bucket custody bucket funding the spend
     * @param amountSats authorized principal amount in satoshis
     * @param destination verified recipient destination
     * @param completedAt signing completion time
     * @param expiresAt expiry of the accepted signing result
     */
    record AcceptedPsbt(
        String intentId,
        String proposalHash,
        String psbtHash,
        String signedPsbtBase64,
        String constitutionHash,
        long constitutionEpoch,
        String[] participantIds,
        int threshold,
        String transcriptHash,
        String aggregateProof,
        String signerKeyId,
        String network,
        String bucket,
        long amountSats,
        String destination,
        Instant completedAt,
        Instant expiresAt
    ) implements VaultMeshPsbtResult {
        /** Requires sufficient signers and the cryptographic evidence for verification.
         * @throws IllegalArgumentException if intent, proposal, signature, quorum, or timestamp data is invalid
         */
        public AcceptedPsbt {
            if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
            if (proposalHash == null || proposalHash.isBlank()) throw new IllegalArgumentException("proposalHash required");
            if (signedPsbtBase64 == null || signedPsbtBase64.isBlank()) throw new IllegalArgumentException("signedPsbtBase64 required");
            if (constitutionHash == null || constitutionHash.isBlank()) throw new IllegalArgumentException("constitutionHash required");
            if (participantIds == null || participantIds.length < threshold) throw new IllegalArgumentException("insufficient participants");
            if (threshold < 1) throw new IllegalArgumentException("threshold must be >= 1");
            if (aggregateProof == null || aggregateProof.isBlank()) throw new IllegalArgumentException("aggregateProof required");
            if (signerKeyId == null || signerKeyId.isBlank()) throw new IllegalArgumentException("signerKeyId required");
            if (completedAt == null) throw new IllegalArgumentException("completedAt required");
        }
    }

    /** Rejection explaining why a PSBT failed policy or transaction validation.
     * @param intentId submitted intent identifier
     * @param proposalHash proposal digest, when it was computed
     * @param reasonCode stable machine-readable rejection code
     * @param reasonDetail safe supplemental explanation
     * @param completedAt rejection time
     */
    record RejectedPsbt(
        String intentId,
        String proposalHash,
        String reasonCode,
        String reasonDetail,
        Instant completedAt
    ) implements VaultMeshPsbtResult {
        /** Requires an intent, reason code, and completion time.
         * @throws IllegalArgumentException if a required value is absent
         */
        public RejectedPsbt {
            if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
            if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reasonCode required");
            if (completedAt == null) throw new IllegalArgumentException("completedAt required");
        }
    }

    /** Fail-stop result indicating signing was halted due to a catastrophic condition.
     * @param intentId submitted intent identifier
     * @param reasonCode stable fail-stop classification
     * @param reasonDetail safe operator-facing explanation
     * @param completedAt time at which the fail-stop was declared
     */
    record FailStopPsbt(
        String intentId,
        String reasonCode,
        String reasonDetail,
        Instant completedAt
    ) implements VaultMeshPsbtResult {
        /** Requires an intent, fail-stop reason, and completion time.
         * @throws IllegalArgumentException if a required value is absent
         */
        public FailStopPsbt {
            if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
            if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reasonCode required");
            if (completedAt == null) throw new IllegalArgumentException("completedAt required");
        }
    }
}

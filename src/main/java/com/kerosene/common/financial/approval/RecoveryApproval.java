package com.kerosene.common.financial.approval;

import java.time.Instant;

/**
 * Recovery proof for emergency access flow.
 * NEVER log or serialize raw fields.
 * @param proof sensitive recovery-key proof bound to the recovery challenge
 * @param challengeNonce one-time challenge nonce covered by the proof
 * @param generatedAt time when the proof was generated
 */
public record RecoveryApproval(
    String proof,
    String challengeNonce,
    Instant generatedAt
) {
    /** Requires a proof, its challenge nonce, and a generation time.
     * @param proof cryptographic recovery proof
     * @param challengeNonce nonce covered by the proof
     * @param generatedAt proof generation time
     * @throws IllegalArgumentException if a required field is absent
     */
    public RecoveryApproval {
        if (proof == null || proof.isBlank()) throw new IllegalArgumentException("proof required");
        if (challengeNonce == null || challengeNonce.isBlank()) throw new IllegalArgumentException("challengeNonce required");
        if (generatedAt == null) throw new IllegalArgumentException("generatedAt required");
    }

    @Override
    /** Returns a summary that omits the sensitive proof.
     * @return redacted recovery approval summary
     */
    public String toString() {
        return "RecoveryApproval[challengeNonce=" + challengeNonce + ", generatedAt=" + generatedAt + "]";
    }
}

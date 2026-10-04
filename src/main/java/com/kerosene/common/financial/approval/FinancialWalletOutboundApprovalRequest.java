package com.kerosene.common.financial.approval;

/**
 * Multi-factor outbound approval with typed secret wrappers.
 * NEVER log or serialize raw factor fields — use {@link #toString()}.
 * @param actorUserId authenticated user initiating the outbound operation
 * @param ownerUserId wallet owner whose approval policy is enforced
 * @param passkeyAssertion WebAuthn proof for the passkey factor
 * @param recoveryApproval recovery proof for the recovery factor
 * @param deviceProof trusted-device proof for the device factor
 */
public record FinancialWalletOutboundApprovalRequest(
        Long actorUserId,
        Long ownerUserId,
        PasskeyAssertion passkeyAssertion,
        RecoveryApproval recoveryApproval,
        DeviceProof deviceProof) {

    /** Requires all identities and factor proofs expected by this request schema.
     * @param actorUserId initiating user identifier
     * @param ownerUserId wallet owner identifier
     * @param passkeyAssertion passkey factor proof
     * @param recoveryApproval recovery factor proof
     * @param deviceProof trusted-device factor proof
     * @throws IllegalArgumentException if any value is absent
     */
    public FinancialWalletOutboundApprovalRequest {
        if (actorUserId == null) throw new IllegalArgumentException("actorUserId required");
        if (ownerUserId == null) throw new IllegalArgumentException("ownerUserId required");
        if (passkeyAssertion == null) throw new IllegalArgumentException("passkeyAssertion required");
        if (recoveryApproval == null) throw new IllegalArgumentException("recoveryApproval required");
        if (deviceProof == null) throw new IllegalArgumentException("deviceProof required");
    }

    @Override
    /** Returns a summary delegated to redacting factor types, never raw proof content.
     * @return redacted request summary
     */
    public String toString() {
        return "FinancialWalletOutboundApprovalRequest[actorUserId=" + actorUserId
                + ", ownerUserId=" + ownerUserId
                + ", passkeyAssertion=" + passkeyAssertion
                + ", recoveryApproval=" + recoveryApproval
                + ", deviceProof=" + deviceProof + "]";
    }
}

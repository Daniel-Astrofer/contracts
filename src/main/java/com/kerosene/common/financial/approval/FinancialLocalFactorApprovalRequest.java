package com.kerosene.common.financial.approval;

/**
 * Local factor approval with typed device proof.
 * NEVER log or serialize raw factor fields.
 * @param userId account user whose local factor is checked
 * @param deviceRef enrolled device reference used to select the factor
 * @param factor cryptographic device proof for the active challenge
 */
public record FinancialLocalFactorApprovalRequest(
        Long userId,
        String deviceRef,
        DeviceProof factor) {

    /** Requires user, device, and proof values before forwarding to the approval port.
     * @param userId account user identifier
     * @param deviceRef enrolled device reference
     * @param factor device-bound proof
     * @throws IllegalArgumentException if any required value is absent
     */
    public FinancialLocalFactorApprovalRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (deviceRef == null || deviceRef.isBlank()) throw new IllegalArgumentException("deviceRef required");
        if (factor == null) throw new IllegalArgumentException("factor required");
    }

    @Override
    /** Returns a diagnostic summary without rendering raw proof material.
     * @return redacted request summary
     */
    public String toString() {
        return "FinancialLocalFactorApprovalRequest[userId=" + userId
                + ", deviceRef=" + deviceRef
                + ", factor=" + factor + "]";
    }
}

package com.kerosene.common.financial.approval;

/**
 * Cold wallet PSBT approval with typed device proof.
 * NEVER log or serialize raw factor fields.
 * @param userId account user authorizing the cold-wallet transaction
 * @param factor device proof bound to the PSBT approval challenge
 */
public record FinancialColdWalletPsbtApprovalRequest(
        Long userId,
        DeviceProof factor) {

    /** Requires both the authorizing user and typed device factor.
     * @param userId authorizing user identifier
     * @param factor device-bound proof
     * @throws IllegalArgumentException if either value is absent
     */
    public FinancialColdWalletPsbtApprovalRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (factor == null) throw new IllegalArgumentException("factor required");
    }

    @Override
    /** Returns a redacted diagnostic summary using the proof type's safe rendering.
     * @return request summary without raw factor contents
     */
    public String toString() {
        return "FinancialColdWalletPsbtApprovalRequest[userId=" + userId
                + ", factor=" + factor + "]";
    }
}

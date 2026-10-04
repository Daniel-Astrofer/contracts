package com.kerosene.common.financial.approval;

/**
 * Custody transfer approval with typed passkey assertion.
 * NEVER log or serialize raw assertion fields.
 * @param userId account user authorizing the custody transfer
 * @param assertion typed WebAuthn assertion bound to the server challenge
 */
public record FinancialCustodyTransferApprovalRequest(
        Long userId,
        PasskeyAssertion assertion) {

    /** Requires both the authorizing user and passkey assertion.
     * @param userId authorizing user identifier
     * @param assertion assertion to verify
     * @throws IllegalArgumentException if either value is absent
     */
    public FinancialCustodyTransferApprovalRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (assertion == null) throw new IllegalArgumentException("assertion required");
    }

    @Override
    /** Returns a redacted summary; nested assertion formatting also masks its contents.
     * @return request summary without raw assertion data
     */
    public String toString() {
        return "FinancialCustodyTransferApprovalRequest[userId=" + userId
                + ", assertion=" + assertion + "]";
    }
}

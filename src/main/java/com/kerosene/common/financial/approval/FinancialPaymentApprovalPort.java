package com.kerosene.common.financial.approval;

/** One request-bound protocol call: challenge if proof absent, acknowledged approval after real verification. */
public interface FinancialPaymentApprovalPort {
    /** Processes a single request-bound approval step.
     * @param request request context, optional factors, and proof when responding to a challenge
     * @return challenge for the device to sign, or acknowledgement after actual proof verification
     */
    FinancialPaymentApprovalV1.Response approve(FinancialPaymentApprovalV1.Request request);
}

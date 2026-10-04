package com.kerosene.common.financial.notification;

import java.util.UUID;

/** Notification details for a deposit credited toward a specific payment request.
 * @param userId payment-request owner
 * @param transactionId transaction that credited the deposit
 * @param paymentRequestId internal payment request identifier
 * @param publicId public payment request identifier shown to the payer
 * @param walletId wallet credited by the deposit
 * @param rail inbound payment rail
 * @param creditedSats credited amount in satoshis
 */
public record FinancialPaymentRequestDepositConfirmedNotificationRequest(
        Long userId,
        UUID transactionId,
        UUID paymentRequestId,
        String publicId,
        UUID walletId,
        String rail,
        long creditedSats) {

    /** Requires payment-request and transaction context plus a positive credited amount.
     * @param userId payment-request owner
     * @param transactionId credited transaction
     * @param paymentRequestId internal request identifier
     * @param publicId payer-facing request identifier
     * @param walletId credited wallet
     * @param rail inbound rail
     * @param creditedSats credited satoshi amount
     * @throws IllegalArgumentException if required values are absent or amount is non-positive
     */
    public FinancialPaymentRequestDepositConfirmedNotificationRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (transactionId == null) throw new IllegalArgumentException("transactionId required");
        if (paymentRequestId == null) throw new IllegalArgumentException("paymentRequestId required");
        if (publicId == null || publicId.isBlank()) throw new IllegalArgumentException("publicId required");
        if (walletId == null) throw new IllegalArgumentException("walletId required");
        if (rail == null || rail.isBlank()) throw new IllegalArgumentException("rail required");
        if (creditedSats <= 0) throw new IllegalArgumentException("creditedSats must be > 0, got: " + creditedSats);
    }
}

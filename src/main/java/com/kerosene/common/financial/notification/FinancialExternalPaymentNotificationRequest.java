package com.kerosene.common.financial.notification;

import java.util.UUID;

/** Notification command for an outbound payment executed on an external rail.
 * @param userId user whose wallet funded the payment
 * @param transactionId platform transaction identifier
 * @param walletId source wallet
 * @param rail external payment rail used to send value
 * @param amountSats positive amount sent, in satoshis
 */
public record FinancialExternalPaymentNotificationRequest(
        Long userId,
        UUID transactionId,
        UUID walletId,
        String rail,
        long amountSats) {

    /** Requires payment identity, rail, and a positive amount.
     * @param userId paying user
     * @param transactionId payment transaction
     * @param walletId source wallet
     * @param rail execution rail
     * @param amountSats sent amount
     * @throws IllegalArgumentException if a required value is absent or amount is non-positive
     */
    public FinancialExternalPaymentNotificationRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (transactionId == null) throw new IllegalArgumentException("transactionId required");
        if (walletId == null) throw new IllegalArgumentException("walletId required");
        if (rail == null || rail.isBlank()) throw new IllegalArgumentException("rail required");
        if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0, got: " + amountSats);
    }
}

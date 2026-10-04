package com.kerosene.common.financial.notification;

import java.util.UUID;

/** Notification command describing a deposit credited after the required confirmation policy.
 * @param userId user receiving the deposit notification
 * @param transactionId platform transaction that recorded the credit
 * @param walletId wallet credited by the deposit
 * @param rail payment rail on which the deposit arrived
 * @param creditedSats positive credited amount in satoshis
 * @param confirmations chain/provider confirmation count observed at notification time
 */
public record FinancialDepositConfirmedNotificationRequest(
        Long userId,
        UUID transactionId,
        UUID walletId,
        String rail,
        long creditedSats,
        int confirmations) {

    /** Requires identifiers, a rail, positive credited value, and non-negative confirmations.
     * @param userId recipient user
     * @param transactionId credited transaction
     * @param walletId credited wallet
     * @param rail source payment rail
     * @param creditedSats credited amount
     * @param confirmations observed confirmations
     * @throws IllegalArgumentException if identifiers are absent or amount/count is invalid
     */
    public FinancialDepositConfirmedNotificationRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (transactionId == null) throw new IllegalArgumentException("transactionId required");
        if (walletId == null) throw new IllegalArgumentException("walletId required");
        if (rail == null || rail.isBlank()) throw new IllegalArgumentException("rail required");
        if (creditedSats <= 0) throw new IllegalArgumentException("creditedSats must be > 0, got: " + creditedSats);
        if (confirmations < 0) throw new IllegalArgumentException("confirmations must be >= 0, got: " + confirmations);
    }
}

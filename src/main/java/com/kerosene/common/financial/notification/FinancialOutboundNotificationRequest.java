package com.kerosene.common.financial.notification;

import java.util.UUID;

/** Legacy notification data for an outbound transfer observed on a financial rail.
 * @param userId user associated with the outbound operation
 * @param transactionId platform transaction identifier
 * @param walletId source wallet identifier
 * @param rail payment rail used to execute the transfer
 * @param amountSats positive amount sent in satoshis
 * @param confirmations current confirmation count
 * @param destinationHint safe destination summary suitable for notification display
 */
public record FinancialOutboundNotificationRequest(
        Long userId,
        UUID transactionId,
        UUID walletId,
        String rail,
        long amountSats,
        int confirmations,
        String destinationHint) {

    /** Requires transaction context and validates positive amount and non-negative confirmations.
     * @param userId related user
     * @param transactionId transaction identifier
     * @param walletId source wallet
     * @param rail execution rail
     * @param amountSats amount in satoshis
     * @param confirmations confirmation count
     * @param destinationHint safe destination summary
     * @throws IllegalArgumentException if an identifier is absent or numeric bounds fail
     */
    public FinancialOutboundNotificationRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (transactionId == null) throw new IllegalArgumentException("transactionId required");
        if (walletId == null) throw new IllegalArgumentException("walletId required");
        if (rail == null || rail.isBlank()) throw new IllegalArgumentException("rail required");
        if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0, got: " + amountSats);
        if (confirmations < 0) throw new IllegalArgumentException("confirmations must be >= 0, got: " + confirmations);
    }
}

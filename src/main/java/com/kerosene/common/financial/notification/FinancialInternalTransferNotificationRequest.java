package com.kerosene.common.financial.notification;

import java.util.UUID;

/** Notification command for an internal ledger transfer.
 * @param userId user associated with the transfer notification
 * @param transactionId internal transfer transaction identifier
 * @param walletId wallet associated with the transfer event
 * @param amountSats positive transfer amount in satoshis
 */
public record FinancialInternalTransferNotificationRequest(
        Long userId,
        UUID transactionId,
        UUID walletId,
        long amountSats) {

    /** Requires identifiers and a positive transfer amount.
     * @param userId related user
     * @param transactionId transfer transaction
     * @param walletId related wallet
     * @param amountSats transfer amount
     * @throws IllegalArgumentException if an identifier is absent or amount is non-positive
     */
    public FinancialInternalTransferNotificationRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (transactionId == null) throw new IllegalArgumentException("transactionId required");
        if (walletId == null) throw new IllegalArgumentException("walletId required");
        if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0, got: " + amountSats);
    }
}

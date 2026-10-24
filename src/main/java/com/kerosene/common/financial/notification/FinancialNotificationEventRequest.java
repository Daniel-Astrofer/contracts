package com.kerosene.common.financial.notification;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/** Immutable financial event accepted durably by the notification subscriber. */
public record FinancialNotificationEventRequest(
        UUID eventId, int schemaVersion, Type type, Long userId,
        UUID transactionId, UUID walletId, String rail, long amountSats,
        int confirmations, String networkReference, String failureCode,
        UUID paymentRequestId, String publicId, Instant occurredAt, long eventSequence) {

    /** Compatibility constructor for already persisted legacy events without sequence numbers. */
    public FinancialNotificationEventRequest(UUID eventId, int schemaVersion, Type type, Long userId,
            UUID transactionId, UUID walletId, String rail, long amountSats, int confirmations,
            String networkReference, String failureCode, UUID paymentRequestId, String publicId, Instant occurredAt) {
        this(eventId, schemaVersion, type, userId, transactionId, walletId, rail, amountSats, confirmations,
                networkReference, failureCode, paymentRequestId, publicId, occurredAt, 0);
    }

    public enum Type {
        DEPOSIT_DETECTED, DEPOSIT_CONFIRMATION_PROGRESS, DEPOSIT_CONFIRMED, DEPOSIT_FINALIZED,
        DEPOSIT_DROPPED, DEPOSIT_CONFLICTED, DEPOSIT_REVERSED, DEPOSIT_RECONCILED, DEPOSIT_UNDERPAID,
        PAYMENT_REQUEST_DEPOSIT_CONFIRMED, INTERNAL_TRANSFER_SENT, INTERNAL_TRANSFER_RECEIVED,
        PAYMENT_PROCESSING, PAYMENT_BROADCAST, PAYMENT_IN_FLIGHT, PAYMENT_CONFIRMED,
        PAYMENT_FAILED, PAYMENT_CONFLICTED, PAYMENT_REPLACED, PAYMENT_RECONCILIATION_REQUIRED
    }

    public FinancialNotificationEventRequest {
        if (eventId == null || type == null || occurredAt == null)
            throw new IllegalArgumentException("Event identity, type and occurrence time are required.");
        if (schemaVersion != 1) throw new IllegalArgumentException("Unsupported financial event version.");
        if (userId == null || userId <= 0 || transactionId == null || walletId == null)
            throw new IllegalArgumentException("Positive recipient and transaction/wallet identifiers are required.");
        if (amountSats <= 0 || confirmations < 0 || eventSequence < 0)
            throw new IllegalArgumentException("Positive amount and non-negative confirmations are required.");
        rail = rail == null ? "" : rail.trim().toUpperCase(Locale.ROOT);
        if (!rail.equals("INTERNAL") && !rail.equals("ONCHAIN") && !rail.equals("LIGHTNING"))
            throw new IllegalArgumentException("Unsupported financial rail.");
        if ((type == Type.INTERNAL_TRANSFER_SENT || type == Type.INTERNAL_TRANSFER_RECEIVED)
                && !rail.equals("INTERNAL")) throw new IllegalArgumentException("Internal event requires INTERNAL rail.");
        if (type == Type.PAYMENT_REQUEST_DEPOSIT_CONFIRMED && paymentRequestId == null)
            throw new IllegalArgumentException("Payment request identity is required.");
        networkReference = bounded(networkReference, 255);
        publicId = bounded(publicId, 48);
        failureCode = bounded(failureCode, 64);
        if (failureCode != null && !failureCode.matches("[A-Z0-9_]+"))
            throw new IllegalArgumentException("Failure code must be a stable category.");
    }

    private static String bounded(String value, int length) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        if (trimmed.length() > length) throw new IllegalArgumentException("Event reference is too long.");
        return trimmed;
    }
}

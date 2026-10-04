package com.kerosene.common.financial.notification;

import java.time.Instant;

/**
 * V2 notification port — single typed event method.
 * Every event is mandatory; no silent no-ops.
 */
public interface FinancialNotificationPortV2 {

    /** Publishes a typed financial event to its user-facing subscriber.
     * @param event event envelope with correlation, ordering, and lifecycle data
     * @return delivery outcome and reason
     * @throws IllegalArgumentException if the event is null or invalid
     */
    DeliveryReceipt publish(FinancialNotificationEvent event);

    /** Outcome returned after an event is accepted or rejected by the subscriber.
     * @param eventId idempotency key of the submitted event
     * @param delivered whether the subscriber accepted delivery
     * @param reasonCode stable reason for the delivery outcome
     */
    record DeliveryReceipt(
        String eventId,       // idempotency key
        boolean delivered,    // was it accepted by the subscriber
        String reasonCode     // why delivery succeeded or failed
    ) {
        /** Requires event identity and a reason code for both success and failure outcomes.
         * @param eventId submitted event id
         * @param delivered subscriber acceptance result
         * @param reasonCode outcome reason
         * @throws IllegalArgumentException if eventId or reasonCode is blank
         */
        public DeliveryReceipt {
            if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
            if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reasonCode required");
        }
    }

    /**
     * Sealed hierarchy of financial events. Every event carries:
     * - eventId for idempotency
     * - transactionId for correlation
     * - eventSequence for ordering
     * - occurredAt for timeline
     * - schemaVersion for compatibility
     */
    sealed interface FinancialNotificationEvent
        permits FinancialNotificationEvent.DepositDetected,
                FinancialNotificationEvent.DepositConfirmed,
                FinancialNotificationEvent.DepositConfirmationProgress,
                FinancialNotificationEvent.DepositDropped,
                FinancialNotificationEvent.PaymentInitiated,
                FinancialNotificationEvent.PaymentBroadcast,
                FinancialNotificationEvent.PaymentConfirmed,
                FinancialNotificationEvent.PaymentFailed,
                FinancialNotificationEvent.PaymentConflicted,
                FinancialNotificationEvent.ReconciliationRequired {

        /** Returns the unique idempotency key for this event.
         * @return event identifier
         */
        String eventId();
        /** Returns the associated financial transaction identifier.
         * @return transaction identifier
         */
        String transactionId();
        /** Returns this event's position in the transaction event sequence.
         * @return monotonically ordered transaction event number
         */
        long eventSequence();
        /** Returns the event occurrence time.
         * @return wall-clock timestamp
         */
        Instant occurredAt();
        /** Returns the event schema revision.
         * @return schema version understood by the consumer
         */
        int schemaVersion();
        /** Returns the identifier linking related operations and notifications.
         * @return correlation identifier, when supplied
         */
        String correlationId();

        // --- Deposit events ---

        /** Inbound deposit observed on a payment rail before final settlement.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt observation time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId wallet expected to receive the funds
         * @param rail inbound rail
         * @param amountSats observed amount
         * @param txid network transaction identifier
         * @param address observed destination address
         */
        record DepositDetected(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, long amountSats, String txid, String address
        ) implements FinancialNotificationEvent {
            /** Validates the event identity, wallet, rail, amount, transaction, and timestamp. */
            public DepositDetected {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (rail == null || rail.isBlank()) throw new IllegalArgumentException("rail required");
                if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0");
                if (txid == null || txid.isBlank()) throw new IllegalArgumentException("txid required");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Inbound Bitcoin or Lightning deposit that has reached its credit threshold.
         * @param eventId unique idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction's event stream
         * @param occurredAt confirmation event time
         * @param schemaVersion event payload schema revision
         * @param correlationId identifier linking related request and notification events
         * @param walletId credited wallet identifier
         * @param rail inbound payment rail
         * @param amountSats credited amount in satoshis
         * @param txid on-chain transaction identifier, or rail-specific payment identifier
         * @param confirmations observed confirmation count
         */
        record DepositConfirmed(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, long amountSats, String txid, int confirmations
        ) implements FinancialNotificationEvent {
            /** Validates event identity, wallet, credited amount, confirmation count, and time. */
            public DepositConfirmed {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0");
                if (confirmations < 0) throw new IllegalArgumentException("confirmations must be >= 0");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Deposit awaiting the configured number of confirmations.
         * @param eventId unique idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction's event stream
         * @param occurredAt progress observation time
         * @param schemaVersion event payload schema revision
         * @param correlationId identifier linking related request and notification events
         * @param walletId wallet expected to receive the deposit
         * @param rail inbound payment rail
         * @param amountSats observed amount in satoshis
         * @param txid on-chain transaction identifier, when available
         * @param confirmations confirmations observed so far
         * @param requiredConfirmations threshold required before credit
         */
        record DepositConfirmationProgress(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, long amountSats, String txid, int confirmations, int requiredConfirmations
        ) implements FinancialNotificationEvent {
            /** Requires an event ID, wallet, timestamp, and a valid current/required confirmation count. */
            public DepositConfirmationProgress {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (confirmations < 0) throw new IllegalArgumentException("confirmations must be >= 0");
                if (requiredConfirmations < 1) throw new IllegalArgumentException("requiredConfirmations must be >= 1");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Deposit observation that was later removed or dropped before confirmation.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt drop observation time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId affected wallet
         * @param txid transaction identifier of the dropped deposit
         * @param reasonCode stable reason for dropping the observation
         * @param reasonDetail safe supplemental explanation
         */
        record DepositDropped(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String txid, String reasonCode, String reasonDetail
        ) implements FinancialNotificationEvent {
            /** Requires event identity, wallet, reason, and occurrence time. */
            public DepositDropped {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reasonCode required");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        // --- Payment events ---

        /** Outbound payment accepted for execution but not yet broadcast.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt initiation time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId source wallet
         * @param rail execution rail
         * @param amountSats principal amount
         * @param feeSats fee amount
         * @param destinationHash digest of the destination
         */
        record PaymentInitiated(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, long amountSats, long feeSats, String destinationHash
        ) implements FinancialNotificationEvent {
            /** Requires event identity, wallet, positive amount, and occurrence time. */
            public PaymentInitiated {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Outbound payment published to its execution network.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt broadcast time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId source wallet
         * @param rail execution rail
         * @param txid transaction or payment identifier returned by the rail
         */
        record PaymentBroadcast(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, String txid
        ) implements FinancialNotificationEvent {
            /** Requires an event ID, wallet, transaction ID, and occurrence time. */
            public PaymentBroadcast {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (txid == null || txid.isBlank()) throw new IllegalArgumentException("txid required");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Outbound payment confirmed by its rail according to the active policy.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt confirmation time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId source wallet
         * @param rail execution rail
         * @param txid transaction identifier of the payment
         * @param confirmations observed confirmation count
         */
        record PaymentConfirmed(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, String txid, int confirmations
        ) implements FinancialNotificationEvent {
            /** Requires event identity, wallet, transaction, non-negative confirmations, and time. */
            public PaymentConfirmed {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (txid == null || txid.isBlank()) throw new IllegalArgumentException("txid required");
                if (confirmations < 0) throw new IllegalArgumentException("confirmations must be >= 0");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Outbound payment that could not complete on its selected rail.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt failure time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId source wallet
         * @param rail selected execution rail
         * @param reasonCode stable failure classification
         * @param reasonDetail safe supplemental failure explanation
         */
        record PaymentFailed(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, String reasonCode, String reasonDetail
        ) implements FinancialNotificationEvent {
            /** Requires event identity, wallet, failure reason, and occurrence time. */
            public PaymentFailed {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reasonCode required");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Outbound payment whose transaction conflicts with another observed transaction.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt conflict observation time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId source wallet
         * @param rail execution rail
         * @param txid original transaction identifier
         * @param conflictingTxid identifier of the conflicting transaction
         */
        record PaymentConflicted(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String rail, String txid, String conflictingTxid
        ) implements FinancialNotificationEvent {
            /** Requires both transaction identifiers and the event's wallet and timestamp. */
            public PaymentConflicted {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (txid == null || txid.isBlank()) throw new IllegalArgumentException("txid required");
                if (conflictingTxid == null || conflictingTxid.isBlank()) throw new IllegalArgumentException("conflictingTxid required");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }

        /** Transaction that cannot be safely finalized without balance/state reconciliation.
         * @param eventId idempotency key
         * @param transactionId platform transaction identifier
         * @param eventSequence order within the transaction event stream
         * @param occurredAt reconciliation-needed time
         * @param schemaVersion event schema revision
         * @param correlationId related-operation correlation identifier
         * @param walletId affected wallet
         * @param reasonCode stable reconciliation reason
         * @param reasonDetail safe supplemental explanation
         */
        record ReconciliationRequired(
            String eventId, String transactionId, long eventSequence, Instant occurredAt, int schemaVersion, String correlationId,
            String walletId, String reasonCode, String reasonDetail
        ) implements FinancialNotificationEvent {
            /** Requires the affected wallet, a reconciliation reason, and occurrence time. */
            public ReconciliationRequired {
                if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
                if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
                if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reasonCode required");
                if (occurredAt == null) throw new IllegalArgumentException("occurredAt required");
            }
        }
    }
}

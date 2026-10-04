package com.kerosene.common.financial.notification;

import java.util.UUID;

/**
 * Dispatches financial lifecycle notifications for deposit, payment, and transfer events.
 */
public interface FinancialNotificationPort {

    /** Reports a deposit credited after confirmation requirements were met.
     * @param userId user receiving the event notification
     * @param transactionId platform transaction identifier for correlation
     * @param walletId wallet associated with the financial event
     * @param rail payment rail on which the event occurred
     * @param creditedSats credited deposit amount in satoshis
     * @param confirmations confirmation count observed when emitted
     */
    default void notifyDepositConfirmed(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long creditedSats,
            int confirmations) {}

    /** Reports a confirmed deposit associated with a payment request.
     * @param userId user receiving the event notification
     * @param transactionId platform transaction identifier for correlation
     * @param paymentRequestId payment request associated with the inbound credit
     * @param publicId public payment request identifier exposed to its payer
     * @param walletId wallet associated with the financial event
     * @param rail payment rail on which the event occurred
     * @param creditedSats credited deposit amount in satoshis
     */
    default void notifyPaymentRequestDepositConfirmed(
            Long userId,
            UUID transactionId,
            UUID paymentRequestId,
            String publicId,
            UUID walletId,
            String rail,
            long creditedSats) {}

    /** Reports initial detection of an unconfirmed inbound deposit.
     * @param userId user receiving the event notification
     * @param transactionId platform transaction identifier for correlation
     * @param walletId wallet receiving the inbound transaction
     * @param rail payment rail on which the transaction was detected
     * @param creditedSats unconfirmed deposit amount in satoshis
     * @param confirmations confirmation count when first observed (typically zero)
     */
    default void notifyDepositDetected(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long creditedSats,
            int confirmations) {}

    /** Reports progress as an inbound deposit accumulates required confirmations.
     * @param userId user receiving the event notification
     * @param transactionId platform transaction identifier for correlation
     * @param walletId wallet receiving the inbound transaction
     * @param rail payment rail on which confirmations are progressing
     * @param creditedSats pending deposit amount in satoshis
     * @param confirmations current confirmation count
     */
    default void notifyDepositConfirmationProgress(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long creditedSats,
            int confirmations) {}

    /** Reports an unconfirmed deposit that was removed from the mempool or invalidated.
     * @param userId user receiving the event notification
     * @param transactionId platform transaction identifier for correlation
     * @param walletId wallet where the pending deposit was observed
     * @param rail payment rail where the transaction was dropped
     * @param amountSats dropped deposit amount in satoshis
     */
    default void notifyDepositDropped(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats) {}

    /** Reports initial on-chain or network observation of an outbound transfer.
     * @param userId user initiating or owning the outbound transfer
     * @param transactionId platform transaction identifier for correlation
     * @param walletId wallet from which value is being debited
     * @param rail payment rail handling the outbound operation
     * @param amountSats transferred amount in satoshis
     * @param confirmations observed confirmation count
     * @param destinationHint redacted or partial destination summary suitable for notification display
     */
    default void notifyOutboundDetected(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats,
            int confirmations,
            String destinationHint) {}

    /** Reports final settlement of an outbound transfer.
     * @param userId user initiating or owning the outbound transfer
     * @param transactionId platform transaction identifier for correlation
     * @param walletId wallet from which value was debited
     * @param rail payment rail handling the outbound operation
     * @param amountSats transferred amount in satoshis
     * @param confirmations final confirmation count observed
     */
    default void notifyOutboundConfirmed(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats,
            int confirmations) {}

    /** Reports value received via an internal platform transfer.
     * @param receiverUserId user receiving value
     * @param transactionId platform transaction identifier for correlation
     * @param walletId recipient wallet
     * @param amountSats transferred amount in satoshis
     */
    default void notifyInternalTransferReceived(
            Long receiverUserId,
            UUID transactionId,
            UUID walletId,
            long amountSats) {}

    /** Reports value transferred internally to another platform account.
     * @param senderUserId user sending value
     * @param transactionId platform transaction identifier for correlation
     * @param walletId source wallet
     * @param amountSats transferred amount in satoshis
     */
    default void notifyInternalTransferSent(
            Long senderUserId,
            UUID transactionId,
            UUID walletId,
            long amountSats) {}

    /** Reports successful submission of a payment to an external rail.
     * @param userId user sending value
     * @param transactionId platform transaction identifier for correlation
     * @param walletId source wallet
     * @param rail external payment rail used
     * @param amountSats transferred amount in satoshis
     */
    default void notifyExternalPaymentSent(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats) {}

    /** Reports that payment processing has been initiated by the platform.
     * @param userId user initiating the payment
     * @param transactionId platform transaction identifier for correlation
     * @param walletId source wallet
     * @param rail selected payment rail
     * @param amountSats outbound amount in satoshis
     */
    default void notifyPaymentInitiated(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats) {}

    /** Reports that a signed transaction was broadcast to its external network.
     * @param userId user initiating the payment
     * @param transactionId platform transaction identifier for correlation
     * @param walletId source wallet
     * @param rail broadcast rail
     * @param amountSats outbound amount in satoshis
     * @param txid network transaction identifier
     */
    default void notifyPaymentBroadcast(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats,
            String txid) {}

    /** Reports confirmation of an outbound payment on its external network.
     * @param userId user initiating the payment
     * @param transactionId platform transaction identifier for correlation
     * @param walletId source wallet
     * @param rail payment rail
     * @param amountSats confirmed amount in satoshis
     * @param confirmations observed confirmation count
     */
    default void notifyPaymentConfirmed(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats,
            int confirmations) {}

    /** Reports permanent or terminal failure of an outbound payment attempt.
     * @param userId user initiating the payment
     * @param transactionId platform transaction identifier for correlation
     * @param walletId source wallet
     * @param rail payment rail
     * @param amountSats intended payment amount in satoshis
     * @param failureCode stable category describing why the payment could not complete
     * @param failureMessage operator- or client-safe explanation of the failure
     */
    default void notifyPaymentFailed(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats,
            String failureCode,
            String failureMessage) {}

    /** Reports an out-of-band discrepancy requiring operator or automated reconciliation.
     * @param userId user owning the affected wallet or transfer
     * @param transactionId platform transaction identifier for correlation
     * @param walletId affected wallet
     * @param rail payment rail where discrepancy was detected
     * @param amountSats disputed or unresolved amount in satoshis
     * @param reason stable explanation of why reconciliation is required
     */
    default void notifyPaymentReconciliationRequired(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats,
            String reason) {}

    /** Reports a conflicting replacement or double-spend detected for an outbound transaction.
     * @param userId user initiating or owning the affected transfer
     * @param transactionId platform transaction identifier for correlation
     * @param walletId source wallet
     * @param rail payment rail observing the conflict
     * @param amountSats amount in satoshis of the original transaction
     * @param txid conflicting or superseding transaction identifier
     */
    default void notifyOutboundConflicted(
            Long userId,
            UUID transactionId,
            UUID walletId,
            String rail,
            long amountSats,
            String txid) {}
}

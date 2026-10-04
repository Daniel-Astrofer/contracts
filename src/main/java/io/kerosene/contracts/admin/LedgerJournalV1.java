package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Admin/core: ledger journal entry for financial reconciliation.
 * <p>
 * {@code amountSats} replaces the old generic {@code amount} + {@code currency} pair.
 * JSON property names use snake_case.
 *
 * @param contractVersion schema version understood by producer and consumer
 * @param entryId stable identifier for this immutable journal entry
 * @param accountId ledger account affected by the entry
 * @param direction entry direction; only {@code debit} or {@code credit} is accepted
 * @param amountSats positive journal amount in satoshis
 * @param description human-readable explanation of the ledger operation
 * @param reference optional domain reference used to trace the originating operation
 * @param recordedAt time at which the journal entry was recorded
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LedgerJournalV1(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("entry_id") String entryId,
        @JsonProperty("account_id") String accountId,
        String direction,
        @JsonProperty("amount_sats") long amountSats,
        String description,
        String reference,
        @JsonProperty("recorded_at") String recordedAt) {

    /** Validates required identifiers, an allowed direction, and a positive amount. */
    public LedgerJournalV1 {
        if (contractVersion == null || contractVersion.isBlank())
            throw new IllegalArgumentException("contractVersion required");
        if (entryId == null || entryId.isBlank())
            throw new IllegalArgumentException("entryId required");
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId required");
        if (direction == null || direction.isBlank())
            throw new IllegalArgumentException("direction required");
        if (!direction.equals("debit") && !direction.equals("credit"))
            throw new IllegalArgumentException("direction must be 'debit' or 'credit', got: " + direction);
        if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0");
    }
}

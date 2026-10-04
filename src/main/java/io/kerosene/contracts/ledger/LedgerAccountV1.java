package io.kerosene.contracts.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * Ledger account for core financial accounting.
 * <p>
 * Uses a multi-saldo satoshi model — all balances are denominated in sats
 * (1 sat = 1e-8 BTC). {@code stateRoot} is the hex-encoded SHA-256 of the
 * account's internal state and is opaque to the wire protocol.
 *
 * @param contractVersion schema version understood by producer and consumer
 * @param accountId stable ledger account identifier
 * @param accountType account category used to interpret the balance buckets
 * @param availableSats confirmed amount available for immediate ledger spending
 * @param reservedSats amount reserved for in-flight operations
 * @param pendingIncomingSats inbound amount not yet fully settled
 * @param pendingOutgoingSats outbound amount not yet fully settled
 * @param confirmedOnchainSats confirmed Bitcoin-chain balance in satoshis
 * @param unconfirmedOnchainSats observed but unconfirmed Bitcoin-chain balance
 * @param spendableByKeroseneSats amount currently spendable under platform policy
 * @param stateVersion monotonic version of the account state snapshot
 * @param stateRoot lowercase SHA-256 hex digest committing to the opaque account state
 * @param tags bounded labels attached to the account for administration or filtering
 * @param createdAt account creation timestamp in the producer's wire format
 * @param updatedAt most recent account update timestamp in the producer's wire format
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LedgerAccountV1(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("account_id") String accountId,
        @JsonProperty("account_type") String accountType,
        @JsonProperty("available_sats") long availableSats,
        @JsonProperty("reserved_sats") long reservedSats,
        @JsonProperty("pending_incoming_sats") long pendingIncomingSats,
        @JsonProperty("pending_outgoing_sats") long pendingOutgoingSats,
        @JsonProperty("confirmed_onchain_sats") long confirmedOnchainSats,
        @JsonProperty("unconfirmed_onchain_sats") long unconfirmedOnchainSats,
        @JsonProperty("spendable_by_kerosene_sats") long spendableByKeroseneSats,
        @JsonProperty("state_version") long stateVersion,
        @JsonProperty("state_root") String stateRoot,
        List<String> tags,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt) {

    /** Validates account identity, satoshi bounds, state digest, tags, and timestamps. */
    public LedgerAccountV1 {
        if (contractVersion == null || contractVersion.isBlank())
            throw new IllegalArgumentException("contractVersion required");
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId required");
        if (accountId.length() > 128)
            throw new IllegalArgumentException("accountId must be at most 128 characters");
        if (accountType == null || accountType.isBlank() || accountType.length() > 64)
            throw new IllegalArgumentException("accountType must be 1-64 characters");
        if (availableSats < 0) throw new IllegalArgumentException("availableSats must be >= 0");
        if (reservedSats < 0) throw new IllegalArgumentException("reservedSats must be >= 0");
        if (pendingIncomingSats < 0) throw new IllegalArgumentException("pendingIncomingSats must be >= 0");
        if (pendingOutgoingSats < 0) throw new IllegalArgumentException("pendingOutgoingSats must be >= 0");
        if (confirmedOnchainSats < 0) throw new IllegalArgumentException("confirmedOnchainSats must be >= 0");
        if (unconfirmedOnchainSats < 0) throw new IllegalArgumentException("unconfirmedOnchainSats must be >= 0");
        if (spendableByKeroseneSats < 0) throw new IllegalArgumentException("spendableByKeroseneSats must be >= 0");
        if (stateVersion < 0) throw new IllegalArgumentException("stateVersion must be >= 0");
        if (stateRoot == null || stateRoot.length() != 64)
            throw new IllegalArgumentException("stateRoot must be 64-char hex digest");
        if (tags != null && tags.size() > 32)
            throw new IllegalArgumentException("tags list must have at most 32 elements");
        if (createdAt == null || createdAt.isBlank())
            throw new IllegalArgumentException("createdAt required");
        if (updatedAt == null || updatedAt.isBlank())
            throw new IllegalArgumentException("updatedAt required");
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}

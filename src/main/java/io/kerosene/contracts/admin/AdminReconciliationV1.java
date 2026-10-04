package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Administrative snapshot comparing the ledger with on-chain and Lightning balances.
 * Satoshi totals are non-negative; {@code deltaSats} communicates the signed difference
 * using the producer's reconciliation convention. The record uses snake_case on the wire.
 *
 * @param contractVersion schema version understood by the producer and consumer
 * @param requestId identifier used to correlate this response with its request
 * @param reconciliationId stable identifier for the reconciliation run
 * @param ledgerSats total balance recorded in the internal ledger, in satoshis
 * @param onchainSats balance observed on the Bitcoin chain, in satoshis
 * @param lightningSats balance observed on the Lightning rail, in satoshis
 * @param deltaSats signed reconciliation difference in satoshis
 * @param status outcome or lifecycle state of the reconciliation
 * @param reconciledAt timestamp when the comparison was completed, when available
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminReconciliationV1(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("reconciliation_id") String reconciliationId,
        @JsonProperty("ledger_sats") long ledgerSats,
        @JsonProperty("onchain_sats") long onchainSats,
        @JsonProperty("lightning_sats") long lightningSats,
        @JsonProperty("delta_sats") long deltaSats,
        String status,
        @JsonProperty("reconciled_at") String reconciledAt) {

    /** Validates required identifiers, non-negative input balances, and status. */
    public AdminReconciliationV1 {
        if (contractVersion == null || contractVersion.isBlank())
            throw new IllegalArgumentException("contractVersion required");
        if (requestId == null || requestId.isBlank())
            throw new IllegalArgumentException("requestId required");
        if (reconciliationId == null || reconciliationId.isBlank())
            throw new IllegalArgumentException("reconciliationId required");
        if (ledgerSats < 0) throw new IllegalArgumentException("ledgerSats must be >= 0");
        if (onchainSats < 0) throw new IllegalArgumentException("onchainSats must be >= 0");
        if (lightningSats < 0) throw new IllegalArgumentException("lightningSats must be >= 0");
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status required");
    }
}

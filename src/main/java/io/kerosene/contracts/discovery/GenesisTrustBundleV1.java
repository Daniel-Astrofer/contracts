package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bootstrap trust roots for bank and vault planes on a network.
 */
public record GenesisTrustBundleV1(
    @JsonProperty("contract_version") String contractVersion,
    @JsonProperty("network_id") String networkId,
    @JsonProperty("bank") TrustPlane bank,
    @JsonProperty("vault") TrustPlane vault,
    @JsonProperty("created_at_epoch_ms") long createdAtEpochMs
) {
    public GenesisTrustBundleV1 {
        if (contractVersion == null || contractVersion.isBlank()) throw new IllegalArgumentException("contractVersion required");
        if (networkId == null || networkId.isBlank()) throw new IllegalArgumentException("networkId required");
        if (bank == null) throw new IllegalArgumentException("bank required");
        if (vault == null) throw new IllegalArgumentException("vault required");
        if (createdAtEpochMs < 0) throw new IllegalArgumentException("createdAtEpochMs must be >= 0");
    }
}

package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bootstrap trust roots for bank and vault planes on a network.
 *
 * @param contractVersion wire format identifier for bootstrap consumers
 * @param networkId identifier of the network whose roots are trusted
 * @param bank initial trust roots and quorum threshold for the bank plane
 * @param vault initial trust roots and quorum threshold for the vault plane
 * @param createdAtEpochMs creation time in Unix epoch milliseconds
 */
public record GenesisTrustBundleV1(
    @JsonProperty("contract_version") String contractVersion,
    @JsonProperty("network_id") String networkId,
    @JsonProperty("bank") TrustPlane bank,
    @JsonProperty("vault") TrustPlane vault,
    @JsonProperty("created_at_epoch_ms") long createdAtEpochMs
) {
    /** Requires a version, network, both trust planes, and a non-negative creation time. */
    public GenesisTrustBundleV1 {
        if (contractVersion == null || contractVersion.isBlank()) throw new IllegalArgumentException("contractVersion required");
        if (networkId == null || networkId.isBlank()) throw new IllegalArgumentException("networkId required");
        if (bank == null) throw new IllegalArgumentException("bank required");
        if (vault == null) throw new IllegalArgumentException("vault required");
        if (createdAtEpochMs < 0) throw new IllegalArgumentException("createdAtEpochMs must be >= 0");
    }
}

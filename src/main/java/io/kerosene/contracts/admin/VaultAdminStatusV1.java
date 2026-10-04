package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/** Versioned operator view of Vault readiness and key-ceremony configuration.
 * @param contractVersion schema version understood by producer and consumer
 * @param requestId request correlation identifier
 * @param localReady whether the local vault process passed its readiness checks
 * @param financialReady whether the vault can safely serve financial signing requests
 * @param nodeId vault node identifier
 * @param ceremonyMode configured distributed key ceremony mode
 * @param bitcoinNetwork Bitcoin network associated with the node's wallet material
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VaultAdminStatusV1(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("local_ready") boolean localReady,
        @JsonProperty("financial_ready") boolean financialReady,
        @JsonProperty("node_id") String nodeId,
        @JsonProperty("ceremony_mode") String ceremonyMode,
        @JsonProperty("bitcoin_network") String bitcoinNetwork) {}

package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/** Versioned operator view of a node's membership, quorum, and financial readiness.
 * @param contractVersion schema version understood by producer and consumer
 * @param requestId request correlation identifier
 * @param networkId distributed membership network identifier
 * @param plane node plane or role in the distributed system
 * @param localReady whether local startup checks passed
 * @param memberReady whether this node is eligible as a network member
 * @param quorumReady whether the configured quorum is available
 * @param financialReady whether financial services are safe to serve
 * @param liveMembers number of currently reachable members
 * @param threshold configured quorum threshold
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NodeAdminStatusV1(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("network_id") String networkId,
        String plane,
        @JsonProperty("local_ready") boolean localReady,
        @JsonProperty("member_ready") boolean memberReady,
        @JsonProperty("quorum_ready") boolean quorumReady,
        @JsonProperty("financial_ready") boolean financialReady,
        @JsonProperty("live_members") long liveMembers,
        int threshold) {}

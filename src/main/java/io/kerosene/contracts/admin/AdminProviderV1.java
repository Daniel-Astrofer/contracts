package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Administrative health and identity snapshot for a financial provider.
 * JSON property names use snake_case; optional heartbeat and version data may be absent.
 *
 * @param contractVersion schema version understood by producer and consumer
 * @param requestId identifier correlating this snapshot to the administrative request
 * @param providerId stable identifier for the provider instance or integration
 * @param providerType provider category, such as a financial rail or custody adapter
 * @param isOnline whether the provider currently reports itself available
 * @param lastHeartbeat timestamp of the last provider health signal, when available
 * @param version provider software or protocol version, when reported
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminProviderV1(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_type") String providerType,
        @JsonProperty("is_online") boolean isOnline,
        @JsonProperty("last_heartbeat") String lastHeartbeat,
        String version) {

    /** Ensures version and provider identifiers are present. */
    public AdminProviderV1 {
        if (contractVersion == null || contractVersion.isBlank())
            throw new IllegalArgumentException("contractVersion required");
        if (requestId == null || requestId.isBlank())
            throw new IllegalArgumentException("requestId required");
        if (providerId == null || providerId.isBlank())
            throw new IllegalArgumentException("providerId required");
        if (providerType == null || providerType.isBlank())
            throw new IllegalArgumentException("providerType required");
    }
}

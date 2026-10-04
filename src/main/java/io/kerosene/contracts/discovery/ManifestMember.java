package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Manifest member configuration including root public key and endpoint.
 */
public record ManifestMember(
    @JsonProperty("member_id") String memberId,
    @JsonProperty("root_public_key") String rootPublicKey,
    @JsonProperty("endpoint") String endpoint
) {
    public ManifestMember {
        if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("memberId required");
        if (rootPublicKey == null || rootPublicKey.isBlank()) throw new IllegalArgumentException("rootPublicKey required");
        if (endpoint == null || endpoint.isBlank()) throw new IllegalArgumentException("endpoint required");
    }
}

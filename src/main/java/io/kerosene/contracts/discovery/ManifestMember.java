package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Manifest member configuration including root public key and endpoint.
 * @param memberId stable identity referenced by signatures, admission, and peer handshakes
 * @param rootPublicKey canonical public verification key bound to this member identity
 * @param endpoint network endpoint advertised for this member
 */
public record ManifestMember(
    @JsonProperty("member_id") String memberId,
    @JsonProperty("root_public_key") String rootPublicKey,
    @JsonProperty("endpoint") String endpoint
) {
    /** Requires identity, verification key, and endpoint so a manifest entry is routable and verifiable. */
    public ManifestMember {
        if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("memberId required");
        if (rootPublicKey == null || rootPublicKey.isBlank()) throw new IllegalArgumentException("rootPublicKey required");
        if (endpoint == null || endpoint.isBlank()) throw new IllegalArgumentException("endpoint required");
    }
}

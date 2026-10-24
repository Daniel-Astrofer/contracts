package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Member identity and root verification key included in the genesis trust bundle.
 * @param memberId stable identity trusted by the containing genesis bundle
 * @param rootPublicKey root verification key used to validate signed membership artifacts
 */
public record TrustMember(
    @JsonProperty("member_id") String memberId,
    @JsonProperty("root_public_key") String rootPublicKey
) {
    /** Requires a member identity and its root verification key. */
    public TrustMember {
        if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("memberId required");
        if (rootPublicKey == null || rootPublicKey.isBlank()) throw new IllegalArgumentException("rootPublicKey required");
    }
}

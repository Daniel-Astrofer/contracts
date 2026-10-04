package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Member identity and root verification key included in the genesis trust bundle.
 */
public record TrustMember(
    @JsonProperty("member_id") String memberId,
    @JsonProperty("root_public_key") String rootPublicKey
) {
    public TrustMember {
        if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("memberId required");
        if (rootPublicKey == null || rootPublicKey.isBlank()) throw new IllegalArgumentException("rootPublicKey required");
    }
}

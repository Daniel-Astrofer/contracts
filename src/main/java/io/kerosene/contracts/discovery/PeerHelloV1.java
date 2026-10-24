package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signed peer introduction that binds a member identity to a challenge and endpoint.
 *
 * @param contractVersion wire contract identifier understood by the peer
 * @param networkId trust network in which the peer claims membership
 * @param plane membership plane of the announcing peer
 * @param memberId stable manifest identity of the announcing member
 * @param rootPublicKey member root key used to verify the introduction
 * @param challenge verifier-provided nonce bound to prevent replay
 * @param issuedAtEpochMs creation time in Unix epoch milliseconds
 * @param endpoint advertised peer endpoint
 * @param signature member signature over the canonical hello fields
 */
public record PeerHelloV1(
    @JsonProperty("contract_version") String contractVersion,
    @JsonProperty("network_id") String networkId,
    @JsonProperty("plane") DiscoveryPlane plane,
    @JsonProperty("member_id") String memberId,
    @JsonProperty("root_public_key") String rootPublicKey,
    @JsonProperty("challenge") String challenge,
    @JsonProperty("issued_at_epoch_ms") long issuedAtEpochMs,
    @JsonProperty("endpoint") String endpoint,
    @JsonProperty("signature") String signature
) {
    /** Rejects incomplete identity/proof fields and negative issuance time before verification. */
    public PeerHelloV1 {
        if (contractVersion == null || contractVersion.isBlank()) throw new IllegalArgumentException("contractVersion required");
        if (networkId == null || networkId.isBlank()) throw new IllegalArgumentException("networkId required");
        if (plane == null) throw new IllegalArgumentException("plane required");
        if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("memberId required");
        if (rootPublicKey == null || rootPublicKey.isBlank()) throw new IllegalArgumentException("rootPublicKey required");
        if (challenge == null || challenge.isBlank()) throw new IllegalArgumentException("challenge required");
        if (issuedAtEpochMs < 0) throw new IllegalArgumentException("issuedAtEpochMs must be >= 0");
        if (endpoint == null || endpoint.isBlank()) throw new IllegalArgumentException("endpoint required");
        if (signature == null || signature.isBlank()) throw new IllegalArgumentException("signature required");
    }
}

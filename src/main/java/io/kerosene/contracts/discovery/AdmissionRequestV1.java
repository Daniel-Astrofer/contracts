package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signed admission request submitted by a candidate node sponsored by an active member.
 *
 * @param contractVersion wire contract identifier understood by the admission service
 * @param networkId trust network to which the requested membership belongs
 * @param plane bank or vault membership plane being joined
 * @param candidate proposed member identity, root key and advertised endpoint
 * @param sponsorId active member responsible for sponsoring this candidate
 * @param challenge fresh admission challenge bound into the sponsor signature
 * @param issuedAtEpochMs Unix epoch-millisecond timestamp used by freshness policy
 * @param signature sponsor signature over the canonical request fields
 */
public record AdmissionRequestV1(
    @JsonProperty("contract_version") String contractVersion,
    @JsonProperty("network_id") String networkId,
    @JsonProperty("plane") DiscoveryPlane plane,
    @JsonProperty("candidate") ManifestMember candidate,
    @JsonProperty("sponsor_id") String sponsorId,
    @JsonProperty("challenge") String challenge,
    @JsonProperty("issued_at_epoch_ms") long issuedAtEpochMs,
    @JsonProperty("signature") String signature
) {
    /** Rejects incomplete identities, missing proof material, or a negative issuance timestamp. */
    public AdmissionRequestV1 {
        if (contractVersion == null || contractVersion.isBlank()) throw new IllegalArgumentException("contractVersion required");
        if (networkId == null || networkId.isBlank()) throw new IllegalArgumentException("networkId required");
        if (plane == null) throw new IllegalArgumentException("plane required");
        if (candidate == null) throw new IllegalArgumentException("candidate required");
        if (sponsorId == null || sponsorId.isBlank()) throw new IllegalArgumentException("sponsorId required");
        if (challenge == null || challenge.isBlank()) throw new IllegalArgumentException("challenge required");
        if (issuedAtEpochMs < 0) throw new IllegalArgumentException("issuedAtEpochMs must be >= 0");
        if (signature == null || signature.isBlank()) throw new IllegalArgumentException("signature required");
    }
}

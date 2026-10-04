package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Versioned, multi-signed membership manifest for a specific plane and epoch.
 */
public record MembershipManifestV1(
    @JsonProperty("contract_version") String contractVersion,
    @JsonProperty("network_id") String networkId,
    @JsonProperty("plane") DiscoveryPlane plane,
    @JsonProperty("epoch") long epoch,
    @JsonProperty("phase") MembershipPhase phase,
    @JsonProperty("previous_manifest_hash") String previousManifestHash,
    @JsonProperty("threshold") int threshold,
    @JsonProperty("members") List<ManifestMember> members,
    @JsonProperty("next_epoch") Long nextEpoch,
    @JsonProperty("signatures") List<ManifestSignature> signatures
) {
    public MembershipManifestV1 {
        if (contractVersion == null || contractVersion.isBlank()) throw new IllegalArgumentException("contractVersion required");
        if (networkId == null || networkId.isBlank()) throw new IllegalArgumentException("networkId required");
        if (plane == null) throw new IllegalArgumentException("plane required");
        if (epoch < 0) throw new IllegalArgumentException("epoch must be >= 0");
        if (phase == null) throw new IllegalArgumentException("phase required");
        if (previousManifestHash == null || previousManifestHash.isBlank()) throw new IllegalArgumentException("previousManifestHash required");
        if (threshold < 1 || threshold > 65535) throw new IllegalArgumentException("threshold must be between 1 and 65535");
        if (members == null || members.isEmpty()) throw new IllegalArgumentException("members required and cannot be empty");
        members = List.copyOf(members);
        signatures = signatures == null ? List.of() : List.copyOf(signatures);
    }
}

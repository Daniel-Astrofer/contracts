package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Versioned, multi-signed membership manifest for a specific plane and epoch.
 *
 * @param contractVersion wire contract identifier used for compatibility checks
 * @param networkId network whose membership is being described
 * @param plane membership plane governed by this manifest
 * @param epoch monotonically increasing manifest epoch
 * @param phase stable single-set membership or joint transition membership
 * @param previousManifestHash digest linking this epoch to its predecessor
 * @param threshold minimum number of valid member signatures required for approval
 * @param members immutable ordered membership set for the epoch
 * @param nextEpoch optional successor epoch associated with a transition
 * @param signatures signatures collected for the canonical manifest; may be empty while proposed
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
    /** Validates manifest identity, epoch, threshold and membership, then defensively freezes collections. */
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

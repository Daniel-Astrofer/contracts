package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Genesis trust configuration for one membership plane and its voting threshold.
 * @param threshold number of member signatures required to approve an artifact
 * @param members non-empty initial set of trusted identities and root keys
 */
public record TrustPlane(
    @JsonProperty("threshold") int threshold,
    @JsonProperty("members") List<TrustMember> members
) {
    /** Validates the quorum range and defensively copies the non-empty trust set. */
    public TrustPlane {
        if (threshold < 1 || threshold > 65535) throw new IllegalArgumentException("threshold must be between 1 and 65535");
        if (members == null || members.isEmpty()) throw new IllegalArgumentException("members required and cannot be empty");
        members = List.copyOf(members);
    }
}

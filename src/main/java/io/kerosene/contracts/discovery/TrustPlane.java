package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Genesis trust configuration for one membership plane and its voting threshold.
 */
public record TrustPlane(
    @JsonProperty("threshold") int threshold,
    @JsonProperty("members") List<TrustMember> members
) {
    public TrustPlane {
        if (threshold < 1 || threshold > 65535) throw new IllegalArgumentException("threshold must be between 1 and 65535");
        if (members == null || members.isEmpty()) throw new IllegalArgumentException("members required and cannot be empty");
        members = List.copyOf(members);
    }
}

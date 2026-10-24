package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Membership phase during epoch rotation.
 */
public enum MembershipPhase {
    /** Normal epoch with one active membership set. */
    @JsonProperty("stable")
    STABLE("stable"),
    /** Joint-consensus transition where old and new membership sets participate. */
    @JsonProperty("joint")
    JOINT("joint");

    /** Stable lowercase wire representation serialized by Jackson. */
    private final String value;

    /** @param value canonical JSON value associated with this membership phase */
    MembershipPhase(String value) {
        this.value = value;
    }

    /** @return canonical lowercase wire value for this manifest phase */
    public String getValue() {
        return value;
    }
}

package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Membership phase during epoch rotation.
 */
public enum MembershipPhase {
    @JsonProperty("stable")
    STABLE("stable"),
    @JsonProperty("joint")
    JOINT("joint");

    private final String value;

    MembershipPhase(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

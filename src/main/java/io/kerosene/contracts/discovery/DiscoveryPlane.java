package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Membership plane whose nodes share one trust and discovery configuration.
 */
public enum DiscoveryPlane {
    @JsonProperty("bank")
    BANK("bank"),
    @JsonProperty("vault")
    VAULT("vault");

    private final String value;

    DiscoveryPlane(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

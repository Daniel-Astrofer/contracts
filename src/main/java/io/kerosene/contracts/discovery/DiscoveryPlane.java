package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Membership plane whose nodes share one trust and discovery configuration.
 */
public enum DiscoveryPlane {
    /** Bank-node membership and discovery trust domain. */
    @JsonProperty("bank")
    BANK("bank"),
    /** Vault-node membership and discovery trust domain. */
    @JsonProperty("vault")
    VAULT("vault");

    /** Stable lowercase wire representation serialized by Jackson. */
    private final String value;

    /** @param value canonical JSON value associated with this plane */
    DiscoveryPlane(String value) {
        this.value = value;
    }

    /** @return canonical lowercase wire value for this membership plane */
    public String getValue() {
        return value;
    }
}

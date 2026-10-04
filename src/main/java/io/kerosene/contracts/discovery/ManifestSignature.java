package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Quorum signature validating a membership manifest.
 */
public record ManifestSignature(
    @JsonProperty("signer_id") String signerId,
    @JsonProperty("signature") String signature
) {
    public ManifestSignature {
        if (signerId == null || signerId.isBlank()) throw new IllegalArgumentException("signerId required");
        if (signature == null || signature.isBlank()) throw new IllegalArgumentException("signature required");
    }
}

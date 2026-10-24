package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Quorum signature validating a membership manifest.
 * @param signerId member identity whose root key verifies the signature
 * @param signature encoded signature over the canonical manifest representation
 */
public record ManifestSignature(
    @JsonProperty("signer_id") String signerId,
    @JsonProperty("signature") String signature
) {
    /** Rejects a missing signer identity or signature bytes. */
    public ManifestSignature {
        if (signerId == null || signerId.isBlank()) throw new IllegalArgumentException("signerId required");
        if (signature == null || signature.isBlank()) throw new IllegalArgumentException("signature required");
    }
}

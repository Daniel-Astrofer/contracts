package com.kerosene.common.financial.approval;

import java.time.Instant;

/**
 * Device-bound proof for trusted-device MFA factor.
 * NEVER log or serialize raw fields.
 * @param deviceId identifier of the enrolled device that generated the proof
 * @param proof cryptographic proof bound to the challenge; sensitive authentication material
 * @param challengeNonce one-time server nonce covered by the proof
 * @param generatedAt client or device generation timestamp used for freshness checks
 */
public record DeviceProof(
    String deviceId,
    String proof,
    String challengeNonce,
    Instant generatedAt
) {
    /** Ensures every value needed to verify device possession is present.
     * @param deviceId enrolled device identifier
     * @param proof cryptographic proof
     * @param challengeNonce nonce that the proof must bind to
     * @param generatedAt generation timestamp
     * @throws IllegalArgumentException if any required field is absent
     */
    public DeviceProof {
        if (deviceId == null || deviceId.isBlank()) throw new IllegalArgumentException("deviceId required");
        if (proof == null || proof.isBlank()) throw new IllegalArgumentException("proof required");
        if (challengeNonce == null || challengeNonce.isBlank()) throw new IllegalArgumentException("challengeNonce required");
        if (generatedAt == null) throw new IllegalArgumentException("generatedAt required");
    }

    @Override
    /** Returns a diagnostic summary without exposing the proof bytes.
     * @return redacted device-proof summary
     */
    public String toString() {
        return "DeviceProof[deviceId=" + deviceId + ", challengeNonce=" + challengeNonce + ", generatedAt=" + generatedAt + "]";
    }
}

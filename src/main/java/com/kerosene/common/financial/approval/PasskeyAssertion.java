package com.kerosene.common.financial.approval;

/**
 * Passkey (WebAuthn) assertion for MFA approval.
 * NEVER log or serialize raw fields. Use {@link #toString()} for summaries.
 * @param credentialId registered WebAuthn credential identifier
 * @param clientDataJson encoded client data containing challenge and origin context
 * @param authenticatorData encoded authenticator flags and relying-party data
 * @param signature credential signature over the WebAuthn assertion payload
 * @param userHandle optional account handle returned by the authenticator
 */
public record PasskeyAssertion(
    String credentialId,
    String clientDataJson,
    String authenticatorData,
    String signature,
    String userHandle
) {
    /** Requires the credential, ceremony data, and signature needed for server verification.
     * @param credentialId registered credential identifier
     * @param clientDataJson client ceremony payload
     * @param authenticatorData authenticator response payload
     * @param signature signed assertion bytes
     * @param userHandle optional user handle
     * @throws IllegalArgumentException if any required verification field is absent
     */
    public PasskeyAssertion {
        if (credentialId == null || credentialId.isBlank()) throw new IllegalArgumentException("credentialId required");
        if (clientDataJson == null || clientDataJson.isBlank()) throw new IllegalArgumentException("clientDataJson required");
        if (authenticatorData == null || authenticatorData.isBlank()) throw new IllegalArgumentException("authenticatorData required");
        if (signature == null || signature.isBlank()) throw new IllegalArgumentException("signature required");
    }

    @Override
    /** Summarizes the credential without exposing ceremony data or signature.
     * @return redacted assertion summary
     */
    public String toString() {
        return "PasskeyAssertion[credentialId=" + credentialId + ", userHandle=" + (userHandle != null ? "***" : "null") + "]";
    }
}

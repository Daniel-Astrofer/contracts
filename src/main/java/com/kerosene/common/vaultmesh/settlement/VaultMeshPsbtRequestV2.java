package com.kerosene.common.vaultmesh.settlement;

import com.kerosene.common.vaultmesh.intent.HybridAuthorization;
import com.kerosene.common.vaultmesh.intent.IntentCommitMode;
import java.time.Instant;

/**
 * PSBT signing request cryptographically bound to an intent.
 * The vault MUST reject if the PSBT doesn't match the intent.
 *
 * @param intentId identifier of the previously authorized settlement intent
 * @param intentHash digest of the canonical intent that this PSBT must satisfy
 * @param unsignedPsbtHash digest of the unsigned PSBT expected by the caller
 * @param sessionId signing-session identifier, when associated by the caller
 * @param psbtBase64 unsigned PSBT encoded as Base64
 * @param network Bitcoin network expected for addresses and transaction validation
 * @param policyHash digest of the custody policy authorizing the spend
 * @param constitutionHash digest of the governance constitution applied to signing
 * @param constitutionEpoch governance epoch under which authorization was issued
 * @param maxFeeSats upper fee bound accepted for the transaction, in satoshis
 * @param allowedOutputDescriptors output descriptors that may receive transaction funds
 * @param changeDescriptor descriptor identifying the permitted wallet change output
 * @param expectedInputCount number of inputs expected in the unsigned transaction
 * @param nonce unique request nonce used to prevent replay
 * @param expiresAt deadline after which the vault must reject the request
 * @param commitMode transaction commitment/authorization mode selected for the workflow
 * @param userAuthorization hybrid user signatures authorizing the canonical intent
 */
public record VaultMeshPsbtRequestV2(
    String intentId,
    String intentHash,
    String unsignedPsbtHash,
    String sessionId,
    String psbtBase64,
    String network,
    String policyHash,
    String constitutionHash,
    long constitutionEpoch,
    long maxFeeSats,
    String[] allowedOutputDescriptors,
    String changeDescriptor,
    int expectedInputCount,
    String nonce,
    Instant expiresAt,
    IntentCommitMode commitMode,
    HybridAuthorization userAuthorization
) {
    /** Validates mandatory hashes, payload, network, fee bounds, input count, nonce, and mode. */
    public VaultMeshPsbtRequestV2 {
        if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
        if (intentHash == null || intentHash.isBlank()) throw new IllegalArgumentException("intentHash required");
        if (unsignedPsbtHash == null || unsignedPsbtHash.isBlank()) throw new IllegalArgumentException("unsignedPsbtHash required");
        if (psbtBase64 == null || psbtBase64.isBlank()) throw new IllegalArgumentException("psbtBase64 required");
        if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
        if (policyHash == null || policyHash.isBlank()) throw new IllegalArgumentException("policyHash required");
        if (constitutionHash == null || constitutionHash.isBlank()) throw new IllegalArgumentException("constitutionHash required");
        if (maxFeeSats < 0) throw new IllegalArgumentException("maxFeeSats must be >= 0");
        if (expectedInputCount < 1) throw new IllegalArgumentException("expectedInputCount must be >= 1");
        if (nonce == null || nonce.isBlank()) throw new IllegalArgumentException("nonce required");
        if (commitMode == null) throw new IllegalArgumentException("commitMode required");
    }
}

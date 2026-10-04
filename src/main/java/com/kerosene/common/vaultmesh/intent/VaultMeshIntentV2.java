package com.kerosene.common.vaultmesh.intent;

import java.time.Instant;
import java.util.Optional;

/** Versioned, time-bounded and signed request to spend from Vault Mesh custody.
 * @param schemaVersion wire schema revision, currently 2
 * @param intentId unique identifier used for replay protection and audit correlation
 * @param network chain network for all destination and transaction validation
 * @param asset asset being spent, currently Bitcoin-denominated value
 * @param bucket custody policy bucket funding the spend
 * @param destination recipient address or destination identifier
 * @param amountSats positive principal amount in satoshis
 * @param maxFeeSats maximum transaction fee accepted by the requester
 * @param policyHash digest of the spend policy authorizing the operation
 * @param constitutionHash digest of the governance constitution
 * @param constitutionEpoch revision of the constitution used for authorization
 * @param dayEpoch current vault governance epoch
 * @param nonce single-use challenge value
 * @param issuedAt intent creation time
 * @param expiresAt intent expiry; must follow issuedAt
 * @param unsignedTransactionHash optional digest binding an unsigned transaction
 * @param authorization hybrid user authorization over the canonical intent
 */
public record VaultMeshIntentV2(
    int schemaVersion,
    String intentId,
    String network,
    String asset,
    String bucket,
    String destination,
    long amountSats,
    long maxFeeSats,
    String policyHash,
    String constitutionHash,
    long constitutionEpoch,
    String dayEpoch,
    String nonce,
    Instant issuedAt,
    Instant expiresAt,
    Optional<String> unsignedTransactionHash,
    HybridAuthorization authorization
) {
    /** Signing-domain prefix preventing this intent's signature from being reused by another protocol. */
    public static final String DOMAIN_SEPARATOR = "KEROSENE_VAULT_INTENT_V2";

    /** Enforces version, required identifiers, positive amount, non-negative fee, and bounded lifetime.
     * @param schemaVersion wire schema version
     * @param intentId intent identifier
     * @param network chain network
     * @param asset asset identifier
     * @param bucket custody bucket
     * @param destination recipient
     * @param amountSats spend amount
     * @param maxFeeSats maximum fee
     * @param policyHash policy digest
     * @param constitutionHash constitution digest
     * @param constitutionEpoch governance revision
     * @param dayEpoch governance day epoch
     * @param nonce replay-protection nonce
     * @param issuedAt issue time
     * @param expiresAt expiry time
     * @param unsignedTransactionHash optional unsigned transaction digest
     * @param authorization hybrid signature envelope
     * @throws IllegalArgumentException if any required invariant is violated
     */
    public VaultMeshIntentV2 {
        if (schemaVersion != 2) throw new IllegalArgumentException("schemaVersion must be 2");
        if (intentId == null || intentId.isBlank()) throw new IllegalArgumentException("intentId required");
        if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
        if (asset == null || asset.isBlank()) throw new IllegalArgumentException("asset required");
        if (bucket == null || bucket.isBlank()) throw new IllegalArgumentException("bucket required");
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("destination required");
        if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0");
        if (maxFeeSats < 0) throw new IllegalArgumentException("maxFeeSats must be >= 0");
        if (policyHash == null || policyHash.isBlank()) throw new IllegalArgumentException("policyHash required");
        if (constitutionHash == null || constitutionHash.isBlank()) throw new IllegalArgumentException("constitutionHash required");
        if (constitutionEpoch < 0) throw new IllegalArgumentException("constitutionEpoch must be >= 0");
        if (dayEpoch == null || dayEpoch.isBlank()) throw new IllegalArgumentException("dayEpoch required");
        if (nonce == null || nonce.isBlank()) throw new IllegalArgumentException("nonce required");
        if (issuedAt == null) throw new IllegalArgumentException("issuedAt required");
        if (expiresAt == null) throw new IllegalArgumentException("expiresAt required");
        if (!expiresAt.isAfter(issuedAt)) throw new IllegalArgumentException("expiresAt must be after issuedAt");
        if (unsignedTransactionHash == null) {
            unsignedTransactionHash = Optional.empty();
        }
        if (authorization == null) throw new IllegalArgumentException("authorization required");
    }

    /** Overloaded constructor accepting a nullable raw transaction hash string instead of an Optional wrapper. */
    public VaultMeshIntentV2(
        int schemaVersion,
        String intentId,
        String network,
        String asset,
        String bucket,
        String destination,
        long amountSats,
        long maxFeeSats,
        String policyHash,
        String constitutionHash,
        long constitutionEpoch,
        String dayEpoch,
        String nonce,
        Instant issuedAt,
        Instant expiresAt,
        String unsignedTransactionHash,
        HybridAuthorization authorization
    ) {
        this(schemaVersion, intentId, network, asset, bucket, destination, amountSats, maxFeeSats,
             policyHash, constitutionHash, constitutionEpoch, dayEpoch, nonce, issuedAt, expiresAt,
             Optional.ofNullable(unsignedTransactionHash), authorization);
    }
}

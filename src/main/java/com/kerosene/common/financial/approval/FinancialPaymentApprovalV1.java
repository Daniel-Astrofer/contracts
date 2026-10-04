package com.kerosene.common.financial.approval;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/** Wire contract and canonicalization rules for request-bound Device Key approval.
 * This protocol binds proofs to one outbound proposal; it is neither a final-price/quorum
 * approval nor a reusable authorization receipt.
 */
public final class FinancialPaymentApprovalV1 {
    /** Internal HTTP route used to request transaction approval. */
    public static final String PATH = "/internal/kfe/transaction-approval/v1/payment";
    /** Proof discriminator identifying the financial Device Key proof format. */
    public static final String PROOF_TYPE = "FINANCIAL_DEVICE_KEY";
    /** Domain-separation purpose included in the signed challenge payload. */
    public static final String PURPOSE = "KFE_PAYMENT_APPROVAL";
    /** Canonical JSON profile used to compute binding hashes and signed payloads. */
    public static final String CANONICALIZATION = "KEROSENE_JSON_V1";
    /** Prevents construction of this constants-and-wire-types holder. */
    private FinancialPaymentApprovalV1() {}

    /** Payment attributes cryptographically bound to the device approval challenge.
     * Optional values remain part of the canonical object, including explicit nulls.
     * @param userId authenticated user identifier within the safe integer range
     * @param deviceRef registered device reference being used to approve
     * @param idempotencyKey request key used to prevent duplicate payment operations
     * @param rail payment rail: INTERNAL, ONCHAIN, or LIGHTNING
     * @param direction INTERNAL for ledger transfers or OUTBOUND for external rails
     * @param sourceWalletId wallet funding the operation, when applicable
     * @param destinationWalletId internal recipient wallet, when applicable
     * @param amountSats principal amount in satoshis
     * @param networkFeeSats quoted network fee in satoshis
     * @param externalReference external transaction reference, when applicable
     * @param memo optional user-supplied payment memo
     * @param paymentRequestPublicId public payment request identifier, when applicable
     * @param feeRateSatPerVbyte selected fee rate, when applicable
     * @param feeTargetBlocks confirmation target for fee estimation, when applicable
     * @param quoteId identifier of the fee quote bound to the request, when applicable
     */
    public record Context(long userId, String deviceRef, String idempotencyKey, String rail, String direction,
            String sourceWalletId, String destinationWalletId, long amountSats, long networkFeeSats,
            String externalReference, String memo, String paymentRequestPublicId,
            Long feeRateSatPerVbyte, Integer feeTargetBlocks, String quoteId) {
        /** Enforces protocol enums, safe integer bounds, and per-field input limits. */
        public Context {
            if (userId <= 0 || userId > 9_007_199_254_740_991L) { throw invalid(); }
            required(deviceRef, 128); required(idempotencyKey, 180);
            if (!Set.of("INTERNAL", "ONCHAIN", "LIGHTNING").contains(Objects.requireNonNull(rail))
                    || !("INTERNAL".equals(rail) ? "INTERNAL".equals(direction) : "OUTBOUND".equals(direction))) { throw invalid(); }
            if (amountSats <= 0 || amountSats > 2_100_000_000_000_000L || networkFeeSats < 0
                    || networkFeeSats > 2_100_000_000_000_000L) { throw invalid(); }
            optional(sourceWalletId, 128); optional(destinationWalletId, 128); optional(externalReference, 8192);
            optional(memo, 255); optional(paymentRequestPublicId, 180); optional(quoteId, 256);
            if (feeRateSatPerVbyte != null && (feeRateSatPerVbyte < -9_007_199_254_740_991L
                    || feeRateSatPerVbyte > 9_007_199_254_740_991L)) { throw invalid(); }
        }
        /** Reports whether this rail requires the app PIN factor in addition to the device proof.
         * @return false only for Lightning payments
         */
        public boolean requiresPin() { return !"LIGHTNING".equals(rail); }
        @Override public String toString() { return "FinancialPaymentContextV1[REDACTED]"; }
    }

    /** Device signature submitted to prove approval of the request-bound challenge.
     * @param version proof schema version, currently 1
     * @param type proof discriminator, currently {@link #PROOF_TYPE}
     * @param credentialId credential whose public key verifies the signature
     * @param deviceInstallId enrolled app installation identity
     * @param signedPayload canonical payload whose bytes were signed
     * @param signature encoded signature over signedPayload
     */
    public record Proof(int version, String type, String credentialId, String deviceInstallId,
            String signedPayload, String signature) {
        /** Requires the supported proof version/type and bounded non-empty proof fields. */
        public Proof {
            if (version != 1 || !PROOF_TYPE.equals(type)) { throw invalid(); }
            required(credentialId, 128); required(deviceInstallId, 128); required(signedPayload, 8192); required(signature, 128);
        }
        @Override public String toString() { return "FinancialPaymentProofV1[REDACTED]"; }
    }

    /** Client request containing payment context, optional local factors, and device proof.
     * @param version request schema version, currently 1
     * @param context payment attributes to bind and authorize
     * @param appPin app PIN factor where required by the rail
     * @param totpCode time-based one-time password, when required
     * @param confirmationPassphrase explicit passphrase confirmation, when required
     * @param proof device-key signature proof for the challenge
     */
    public record Request(int version, Context context, String appPin, String totpCode,
            String confirmationPassphrase, Proof proof) {
        /** Requires supported version and context, and enforces the optional factor size bounds. */
        public Request {
            if (version != 1 || context == null) { throw invalid(); }
            optional(appPin, 128); optional(totpCode, 128); optional(confirmationPassphrase, 4096);
        }
        @Override public String toString() { return "FinancialPaymentRequestV1[REDACTED]"; }
    }

    /** Server challenge that binds one device signature to a payment context and short validity window.
     * @param version challenge schema version, currently 1
     * @param purpose domain-separation purpose
     * @param challengeId one-time challenge identifier
     * @param challenge cryptographic challenge value as lowercase hex
     * @param bindingHash digest of the canonical payment context
     * @param username account name associated with the challenge
     * @param onionServiceId authenticated service identity for the challenge origin
     * @param issuedAtEpochSeconds challenge issue time
     * @param expiresAtEpochSeconds expiry time, at most 120 seconds after issue
     * @param algorithm signature algorithm required from the device
     * @param canonicalization canonical serialization profile for signed bytes
     */
    public record Challenge(int version, String purpose, String challengeId, String challenge,
            String bindingHash, String username, String onionServiceId, long issuedAtEpochSeconds,
            long expiresAtEpochSeconds, String algorithm, String canonicalization) {
        /** Enforces protocol identifiers, lowercase digest encoding, algorithm, and lifetime bounds. */
        public Challenge {
            if (version != 1 || !PURPOSE.equals(purpose) || !"Ed25519".equals(algorithm)
                    || !CANONICALIZATION.equals(canonicalization)) { throw invalid(); }
            required(challengeId, 128); hex(challenge); hex(bindingHash); required(username, 255); required(onionServiceId, 255);
            if (issuedAtEpochSeconds <= 0 || expiresAtEpochSeconds > 9_007_199_254_740_991L
                    || expiresAtEpochSeconds <= issuedAtEpochSeconds
                    || expiresAtEpochSeconds - issuedAtEpochSeconds > 120) { throw invalid(); }
        }
        @Override public String toString() { return "FinancialPaymentChallengeV1[REDACTED]"; }
    }

    /** Server response representing either an issued challenge or completed approval.
     * @param version response schema version, currently 1
     * @param status response state: CHALLENGE or APPROVED
     * @param bindingHash digest of the request context being approved
     * @param approvalId resulting approval identifier, present only after approval
     * @param expiresAtEpochSeconds challenge or approval expiry
     * @param challenge challenge body, present only for CHALLENGE status
     */
    public record Response(int version, String status, String bindingHash, String approvalId,
            long expiresAtEpochSeconds, Challenge challenge) {
        /** Enforces mutually exclusive challenge and approved response shapes. */
        public Response {
            if (version != 1) { throw invalid(); } hex(bindingHash);
            if ("CHALLENGE".equals(status)) {
                if (challenge == null || approvalId != null || !bindingHash.equals(challenge.bindingHash())
                        || expiresAtEpochSeconds != challenge.expiresAtEpochSeconds()) { throw invalid(); }
            } else if ("APPROVED".equals(status)) {
                required(approvalId, 128);
                if (challenge != null || expiresAtEpochSeconds <= 0
                        || expiresAtEpochSeconds > 9_007_199_254_740_991L) { throw invalid(); }
            } else { throw invalid(); }
        }
        @Override public String toString() { return "FinancialPaymentResponseV1[status=" + status + ", REDACTED]"; }
    }

    /** Computes the SHA-256 binding digest over sorted flat JSON, including null-valued fields.
     * This digest is distinct from the historical idempotency fingerprint.
     * @param context validated payment context to bind
     * @return lowercase hexadecimal SHA-256 binding digest
     * @throws IllegalStateException if canonicalization or digest computation fails
     */
    public static String bindingHash(Context context) {
        try {
            var values = new TreeMap<String, Object>();
            values.put("amountSats", context.amountSats()); values.put("destinationWalletId", context.destinationWalletId());
            values.put("deviceRef", context.deviceRef()); values.put("direction", context.direction());
            values.put("externalReference", context.externalReference()); values.put("feeRateSatPerVbyte", context.feeRateSatPerVbyte());
            values.put("feeTargetBlocks", context.feeTargetBlocks()); values.put("idempotencyKey", context.idempotencyKey());
            values.put("memo", context.memo()); values.put("networkFeeSats", context.networkFeeSats());
            values.put("paymentRequestPublicId", context.paymentRequestPublicId()); values.put("quoteId", context.quoteId());
            values.put("rail", context.rail()); values.put("sourceWalletId", context.sourceWalletId());
            values.put("userId", context.userId()); values.put("version", 1); values.put("purpose", PURPOSE);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonicalJson(values).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception failure) { throw new IllegalStateException("Payment binding unavailable"); }
    }

    /** Builds the canonical payload that a device credential must sign for approval.
     * @param challenge server-issued challenge and context binding
     * @param credentialId credential identifier used for verification
     * @param deviceInstallId enrolled installation identity
     * @param counter authenticator signature counter
     * @param issuedAtEpochSeconds client assertion issue time within the challenge lifetime
     * @return canonical compact JSON string to sign
     * @throws IllegalArgumentException if credential data, counter, or timestamp is invalid
     */
    public static String signedPayload(Challenge challenge, String credentialId, String deviceInstallId,
            long counter, long issuedAtEpochSeconds) {
        required(credentialId, 128); required(deviceInstallId, 128);
        if (counter <= 0 || counter > 9_007_199_254_740_991L || issuedAtEpochSeconds < challenge.issuedAtEpochSeconds()
                || issuedAtEpochSeconds >= challenge.expiresAtEpochSeconds()) { throw invalid(); }
        var values = new TreeMap<String, Object>();
        values.put("bindingHash", challenge.bindingHash()); values.put("challenge", challenge.challenge());
        values.put("challengeId", challenge.challengeId()); values.put("counter", counter);
        values.put("credentialId", credentialId); values.put("deviceInstallId", deviceInstallId);
        values.put("issuedAtEpochSeconds", issuedAtEpochSeconds); values.put("onionServiceId", challenge.onionServiceId());
        values.put("type", PURPOSE); values.put("username", challenge.username()); values.put("version", 1);
        return canonicalJson(values);
    }

    /** Flat JSON subset has no floats, nested maps or arrays; escaping matches the shared UTF-8 vector. */
    private static String canonicalJson(TreeMap<String, Object> values) {
        var result = new StringBuilder("{"); boolean first = true;
        for (var entry : values.entrySet()) {
            if (!first) { result.append(','); } first = false;
            appendString(result, entry.getKey()); result.append(':');
            if (entry.getValue() instanceof String text) { appendString(result, text); }
            else { result.append(entry.getValue()); }
        }
        return result.append('}').toString();
    }

    private static void appendString(StringBuilder target, String value) {
        target.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> target.append("\\\"");
                case '\\' -> target.append("\\\\");
                case '\b' -> target.append("\\b");
                case '\f' -> target.append("\\f");
                case '\n' -> target.append("\\n");
                case '\r' -> target.append("\\r");
                case '\t' -> target.append("\\t");
                default -> { if (c < 0x20) { target.append("\\u00").append("0123456789abcdef".charAt(c >> 4)).append("0123456789abcdef".charAt(c & 15)); }
                    else { target.append(c); } }
            }
        }
        target.append('"');
    }

    private static void hex(String value) { if (value == null || !value.matches("[0-9a-f]{64}")) { throw invalid(); } }
    private static void required(String value, int max) { if (value == null || value.isBlank()) { throw invalid(); } optional(value, max); }
    private static void optional(String value, int max) {
        if (value == null) { return; }
        if (value.length() > max) { throw invalid(); }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (++i >= value.length() || !Character.isLowSurrogate(value.charAt(i))) { throw invalid(); }
            } else if (Character.isLowSurrogate(c)) { throw invalid(); }
        }
    }
    private static IllegalArgumentException invalid() { return new IllegalArgumentException("Invalid financial payment approval v1"); }
}

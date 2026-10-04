package com.kerosene.common.financial.approval;

import java.time.Instant;
import java.util.Set;

/**
 * Transactional approval bound to a specific outbound proposal.
 * Approval factors MUST be cryptographically linked to the transaction they authorize.
 */
public interface FinancialTransactionApprovalPort {

    /**
     * Approves an outbound transaction with its factors bound to the proposal.
     * @param challenge immutable transaction details and accepted authentication proofs
     * @return verifiable receipt binding the approved factors to the proposal
     * @throws UnsupportedOperationException when this adapter has not implemented typed approval
     */
    default ApprovalReceipt approveOutbound(OutboundApprovalChallenge challenge) {
        throw new UnsupportedOperationException("Typed outbound approval is not implemented by this adapter");
    }

    /** Approves a transaction with a device-local factor proof.
     * @param userId user whose factor is being verified
     * @param deviceRef enrolled device reference
     * @param factor proof bound to the active challenge
     * @throws UnsupportedOperationException when the adapter lacks typed local-factor support
     */
    default void approveLocalFactor(Long userId, String deviceRef, DeviceProof factor) {
        throw new UnsupportedOperationException("Typed local-factor approval is not implemented");
    }

    /** Approves a custody transfer using a passkey assertion.
     * @param userId user authorizing the transfer
     * @param assertion verified passkey response bound to the server challenge
     * @throws UnsupportedOperationException when the adapter lacks typed custody approval
     */
    default void approveCustodyTransfer(Long userId, PasskeyAssertion assertion) {
        throw new UnsupportedOperationException("Typed custody approval is not implemented");
    }

    /** Approves an outbound wallet operation using the supplied independent factors.
     * @param actorUserId authenticated user performing the operation
     * @param ownerUserId wallet owner whose authorization policy applies
     * @param passkeyAssertion passkey proof, when required by policy
     * @param recoveryApproval recovery-factor proof, when required by policy
     * @param deviceProof device-factor proof, when required by policy
     * @throws UnsupportedOperationException when typed wallet approval is not implemented
     */
    default void approveWalletOutbound(
            Long actorUserId,
            Long ownerUserId,
            PasskeyAssertion passkeyAssertion,
            RecoveryApproval recoveryApproval,
            DeviceProof deviceProof) {
        throw new UnsupportedOperationException("Typed wallet approval is not implemented");
    }

    /** Approves a cold-wallet PSBT with a device proof.
     * @param userId user authorizing the transaction
     * @param factor device proof bound to the PSBT challenge
     * @throws UnsupportedOperationException when typed cold-wallet approval is not implemented
     */
    default void approveColdWalletPsbt(Long userId, DeviceProof factor) {
        throw new UnsupportedOperationException("Typed cold-wallet approval is not implemented");
    }

    /**
     * Challenge that binds approval factors to a specific transaction.
     * @param proposalHash digest of the canonical outbound proposal
     * @param transactionId identifier of the transaction being authorized
     * @param ownerUserId user that owns the wallet and approval policy
     * @param actorUserId user initiating the outbound operation
     * @param walletId wallet from which value will be spent
     * @param destinationHash digest of the canonical destination
     * @param amountSats amount being transferred, in satoshis
     * @param feeLimitSats maximum fee accepted by the approver, in satoshis
     * @param network chain/network identifier used for validation
     * @param rail payment rail used to execute the outbound operation
     * @param nonce one-time challenge value used to prevent replay
     * @param issuedAt challenge creation time
     * @param expiresAt challenge expiry; must be later than issue time
     * @param assertions non-empty set of typed authentication factor proofs
     */
    record OutboundApprovalChallenge(
        String proposalHash,
        String transactionId,
        String ownerUserId,
        String actorUserId,
        String walletId,
        String destinationHash,
        long amountSats,
        long feeLimitSats,
        String network,
        String rail,
        String nonce,
        Instant issuedAt,
        Instant expiresAt,
        Set<AuthenticationAssertion> assertions
    ) {
        /** Validates binding identifiers, amounts, challenge lifetime, and non-empty factor proofs.
         * @param proposalHash canonical proposal digest
         * @param transactionId transaction identifier
         * @param ownerUserId wallet owner
         * @param actorUserId requesting actor
         * @param walletId source wallet
         * @param destinationHash destination digest
         * @param amountSats transaction amount
         * @param feeLimitSats maximum permitted fee
         * @param network target network
         * @param rail execution rail
         * @param nonce replay-protection value
         * @param issuedAt challenge issue time
         * @param expiresAt challenge expiry time
         * @param assertions authentication proofs to evaluate
         * @throws IllegalArgumentException if a required value is missing or the time range is invalid
         */
        public OutboundApprovalChallenge {
            if (proposalHash == null || proposalHash.isBlank()) throw new IllegalArgumentException("proposalHash required");
            if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId required");
            if (ownerUserId == null || ownerUserId.isBlank()) throw new IllegalArgumentException("ownerUserId required");
            if (actorUserId == null || actorUserId.isBlank()) throw new IllegalArgumentException("actorUserId required");
            if (walletId == null || walletId.isBlank()) throw new IllegalArgumentException("walletId required");
            if (amountSats <= 0) throw new IllegalArgumentException("amountSats must be > 0");
            if (feeLimitSats <= 0) throw new IllegalArgumentException("feeLimitSats must be > 0");
            if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
            if (rail == null || rail.isBlank()) throw new IllegalArgumentException("rail required");
            if (nonce == null || nonce.isBlank()) throw new IllegalArgumentException("nonce required");
            if (issuedAt == null) throw new IllegalArgumentException("issuedAt required");
            if (expiresAt == null) throw new IllegalArgumentException("expiresAt required");
            if (!expiresAt.isAfter(issuedAt)) throw new IllegalArgumentException("expiresAt must be after issuedAt");
            if (assertions == null || assertions.isEmpty()) throw new IllegalArgumentException("at least one assertion required");
        }
    }

    /**
     * Authentication assertion — proof from a specific factor.
     * Sealed to enforce type safety per factor kind.
     */
    sealed interface AuthenticationAssertion
        permits AuthenticationAssertion.PasskeyAssertion,
                AuthenticationAssertion.DeviceProofAssertion,
                AuthenticationAssertion.RecoveryAssertion,
                AuthenticationAssertion.CustodyAssertion {

        /** Returns the stable discriminator associated with this assertion variant.
         * @return factor type such as {@code PASSKEY} or {@code DEVICE_PROOF}
         */
        String factorType();

        /** WebAuthn assertion proving possession of a registered passkey.
         * @param credentialId registered credential identifier
         * @param clientDataJson encoded client data from the assertion ceremony
         * @param authenticatorData encoded authenticator response data
         * @param signature credential signature over the WebAuthn challenge
         */
        record PasskeyAssertion(
            String credentialId,
            String clientDataJson,
            String authenticatorData,
            String signature
        ) implements AuthenticationAssertion {
            /** Requires every WebAuthn field needed for assertion verification. */
            public PasskeyAssertion {
                if (credentialId == null || credentialId.isBlank()) throw new IllegalArgumentException("credentialId required");
                if (clientDataJson == null || clientDataJson.isBlank()) throw new IllegalArgumentException("clientDataJson required");
                if (authenticatorData == null || authenticatorData.isBlank()) throw new IllegalArgumentException("authenticatorData required");
                if (signature == null || signature.isBlank()) throw new IllegalArgumentException("signature required");
            }
            public String factorType() { return "PASSKEY"; }
        }

        /** Proof that an enrolled device answered the current approval challenge.
         * @param deviceId stable enrolled-device identifier
         * @param proof cryptographic device response
         * @param challengeNonce nonce that the proof must bind to
         */
        record DeviceProofAssertion(
            String deviceId,
            String proof,
            String challengeNonce
        ) implements AuthenticationAssertion {
            /** Requires device identity, proof, and challenge binding. */
            public DeviceProofAssertion {
                if (deviceId == null || deviceId.isBlank()) throw new IllegalArgumentException("deviceId required");
                if (proof == null || proof.isBlank()) throw new IllegalArgumentException("proof required");
                if (challengeNonce == null || challengeNonce.isBlank()) throw new IllegalArgumentException("challengeNonce required");
            }
            public String factorType() { return "DEVICE_PROOF"; }
        }

        /** Proof produced by an enrolled recovery key.
         * @param recoveryKeyId identifier of the recovery key
         * @param proof cryptographic recovery-key response
         * @param challengeNonce nonce that the proof must bind to
         */
        record RecoveryAssertion(
            String recoveryKeyId,
            String proof,
            String challengeNonce
        ) implements AuthenticationAssertion {
            /** Requires recovery-key identity, proof, and challenge binding. */
            public RecoveryAssertion {
                if (recoveryKeyId == null || recoveryKeyId.isBlank()) throw new IllegalArgumentException("recoveryKeyId required");
                if (proof == null || proof.isBlank()) throw new IllegalArgumentException("proof required");
                if (challengeNonce == null || challengeNonce.isBlank()) throw new IllegalArgumentException("challengeNonce required");
            }
            public String factorType() { return "RECOVERY"; }
        }

        /** Proof returned by the custody authorization provider.
         * @param custodyProviderId identifier of the provider that issued the proof
         * @param proof provider-authenticated approval response
         * @param challengeNonce nonce that the proof must bind to
         */
        record CustodyAssertion(
            String custodyProviderId,
            String proof,
            String challengeNonce
        ) implements AuthenticationAssertion {
            /** Requires custody-provider identity, proof, and challenge binding. */
            public CustodyAssertion {
                if (custodyProviderId == null || custodyProviderId.isBlank()) throw new IllegalArgumentException("custodyProviderId required");
                if (proof == null || proof.isBlank()) throw new IllegalArgumentException("proof required");
                if (challengeNonce == null || challengeNonce.isBlank()) throw new IllegalArgumentException("challengeNonce required");
            }
            public String factorType() { return "CUSTODY"; }
        }
    }

    /**
     * Verifiable approval receipt.
     * @param approvalId unique identifier for this approval decision
     * @param proposalHash digest of the approved proposal
     * @param approvedFactors factor discriminators satisfied by the approval
     * @param policyVersion version of the policy used to evaluate the factors
     * @param issuedAt receipt creation time
     * @param expiresAt receipt expiry time; must be later than issue time
     * @param proof cryptographic proof or signature verifiable by the caller
     */
    record ApprovalReceipt(
        String approvalId,
        String proposalHash,
        Set<String> approvedFactors,
        String policyVersion,
        Instant issuedAt,
        Instant expiresAt,
        String proof
    ) {
        /** Requires an identifiable proposal, approved factors, policy, bounded lifetime, and proof.
         * @param approvalId receipt identifier
         * @param proposalHash approved proposal digest
         * @param approvedFactors factors that satisfied policy
         * @param policyVersion policy revision
         * @param issuedAt receipt issue time
         * @param expiresAt receipt expiry time
         * @param proof verifiable authorization proof
         * @throws IllegalArgumentException if any required field is absent or expiry is not later
         */
        public ApprovalReceipt {
            if (approvalId == null || approvalId.isBlank()) throw new IllegalArgumentException("approvalId required");
            if (proposalHash == null || proposalHash.isBlank()) throw new IllegalArgumentException("proposalHash required");
            if (approvedFactors == null || approvedFactors.isEmpty()) throw new IllegalArgumentException("at least one approved factor required");
            if (policyVersion == null || policyVersion.isBlank()) throw new IllegalArgumentException("policyVersion required");
            if (issuedAt == null) throw new IllegalArgumentException("issuedAt required");
            if (expiresAt == null) throw new IllegalArgumentException("expiresAt required");
            if (!expiresAt.isAfter(issuedAt)) throw new IllegalArgumentException("expiresAt must be after issuedAt");
            if (proof == null || proof.isBlank()) throw new IllegalArgumentException("proof required");
        }
    }
}

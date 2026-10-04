package com.kerosene.common.financial.operations;

import java.time.Instant;
import java.util.Set;

/**
 * Constitutional threshold quorum — NOT "healthy unanimity".
 * Every decision is cryptographically attributable to specific members.
 * A successful decision must satisfy the configured threshold; rejection and timeout
 * results retain per-member attribution rather than collapsing to a health count.
 */
public interface FinancialQuorumPort {

    /**
     * Requests threshold-based quorum consensus for a proposal.
     * @param proposal immutable proposal digest and governance epoch to vote on
     * @return QuorumDecision with full attribution and proof
     * @throws UnsupportedOperationException when this adapter has no typed quorum implementation
     */
    default QuorumDecision requireThresholdConsensus(Proposal proposal) {
        throw new UnsupportedOperationException("Constitutional threshold quorum is not implemented by this adapter");
    }

    /**
     * A proposal submitted for quorum voting.
     * @param proposalHash canonical proposal digest
     * @param constitutionHash digest of the governing constitution
     * @param constitutionEpoch constitution revision under which members vote
     * @param submittedAt proposal submission time
     * @param expiresAt deadline after which votes are no longer accepted
     */
    record Proposal(
        String proposalHash,
        String constitutionHash,
        long constitutionEpoch,
        Instant submittedAt,
        Instant expiresAt
    ) {
        /** Validates proposal hashes and requires expiry strictly after submission.
         * @param proposalHash proposal digest
         * @param constitutionHash governing constitution digest
         * @param constitutionEpoch constitution revision
         * @param submittedAt submission time
         * @param expiresAt voting deadline
         * @throws IllegalArgumentException if required values are missing or expiry is not later
         */
        public Proposal {
            if (proposalHash == null || proposalHash.isBlank()) throw new IllegalArgumentException("proposalHash required");
            if (constitutionHash == null || constitutionHash.isBlank()) throw new IllegalArgumentException("constitutionHash required");
            if (constitutionEpoch < 0) throw new IllegalArgumentException("constitutionEpoch must be >= 0");
            if (submittedAt == null) throw new IllegalArgumentException("submittedAt required");
            if (expiresAt == null) throw new IllegalArgumentException("expiresAt required");
            if (!expiresAt.isAfter(submittedAt)) throw new IllegalArgumentException("expiresAt must be after submittedAt");
        }
    }

    /** Mutually exclusive final state for a threshold-based member vote. */
    enum Decision {
        /** The accepted votes reached the configured threshold and carry an aggregate proof. */
        ACCEPTED,
        /** The quorum rejected the proposal. */
        REJECTED,
        /** The vote deadline elapsed before an outcome could be reached. */
        TIMED_OUT
    }

    /**
     * Fully-attributed quorum decision with all verification fields.
     * State invariants are enforced by the compact constructor.
     * @param decision accepted, rejected, or timed-out quorum outcome
     * @param proposalHash digest of the proposal that received votes
     * @param constitutionHash governance constitution digest used for verification
     * @param constitutionEpoch governance revision associated with the decision
     * @param configuredMembers number of members configured in the voting set
     * @param requiredThreshold minimum accepted votes needed for approval
     * @param acceptedMembers member identifiers whose votes accepted the proposal
     * @param rejectedMembers member identifiers whose votes rejected the proposal
     * @param unavailableMembers configured members that did not provide a decision
     * @param aggregateProof cryptographic aggregate proof, required for an accepted result
     * @param decidedAt time the final quorum decision was produced
     */
    record QuorumDecision(
        Decision decision,
        String proposalHash,
        String constitutionHash,
        long constitutionEpoch,
        int configuredMembers,
        int requiredThreshold,
        Set<String> acceptedMembers,
        Set<String> rejectedMembers,
        Set<String> unavailableMembers,
        String aggregateProof,
        Instant decidedAt
    ) {
        /** Enforces member attribution, threshold bounds, and outcome-specific proof rules.
         * @param decision final quorum outcome
         * @param proposalHash voted proposal digest
         * @param constitutionHash governing constitution digest
         * @param constitutionEpoch governance revision
         * @param configuredMembers total configured voters
         * @param requiredThreshold minimum votes for acceptance
         * @param acceptedMembers members that accepted
         * @param rejectedMembers members that rejected
         * @param unavailableMembers members that did not return a decision
         * @param aggregateProof aggregate signature/proof, required for acceptance
         * @param decidedAt final decision time
         * @throws IllegalArgumentException if member attribution or decision invariants fail
         */
        public QuorumDecision {
            if (proposalHash == null || proposalHash.isBlank()) throw new IllegalArgumentException("proposalHash required");
            if (constitutionHash == null || constitutionHash.isBlank()) throw new IllegalArgumentException("constitutionHash required");
            if (decision == null) throw new IllegalArgumentException("decision required");

            if (configuredMembers < 1) throw new IllegalArgumentException("configuredMembers must be >= 1");
            if (requiredThreshold < 1) throw new IllegalArgumentException("requiredThreshold must be >= 1");
            if (requiredThreshold > configuredMembers) throw new IllegalArgumentException("requiredThreshold cannot exceed configuredMembers");
            if (constitutionEpoch < 0) throw new IllegalArgumentException("constitutionEpoch must be >= 0");

            if (acceptedMembers == null) acceptedMembers = Set.of();
            if (rejectedMembers == null) rejectedMembers = Set.of();
            if (unavailableMembers == null) unavailableMembers = Set.of();

            for (String member : acceptedMembers) {
                if (rejectedMembers.contains(member)) throw new IllegalArgumentException("member " + member + " in both accepted and rejected");
                if (unavailableMembers.contains(member)) throw new IllegalArgumentException("member " + member + " in both accepted and unavailable");
            }
            for (String member : rejectedMembers) {
                if (unavailableMembers.contains(member)) throw new IllegalArgumentException("member " + member + " in both rejected and unavailable");
            }

            int totalAttributed = acceptedMembers.size() + rejectedMembers.size() + unavailableMembers.size();
            if (totalAttributed != configuredMembers) throw new IllegalArgumentException(
                "attributed members (" + totalAttributed + ") != configuredMembers (" + configuredMembers + ")"
            );

            switch (decision) {
                case ACCEPTED -> {
                    if (acceptedMembers.size() < requiredThreshold)
                        throw new IllegalArgumentException("ACCEPTED requires acceptedMembers >= " + requiredThreshold + ", got " + acceptedMembers.size());
                    if (aggregateProof == null || aggregateProof.isBlank())
                        throw new IllegalArgumentException("aggregateProof required for ACCEPTED");
                }
                case REJECTED -> {
                    if (rejectedMembers.isEmpty())
                        throw new IllegalArgumentException("REJECTED requires at least one rejectedMember");
                }
                case TIMED_OUT -> {
                    if (unavailableMembers.isEmpty())
                        throw new IllegalArgumentException("TIMED_OUT requires at least one unavailableMember");
                }
            }

            if (decidedAt == null) throw new IllegalArgumentException("decidedAt required");
        }
    }

    /** Evaluates healthy unanimous consensus across active quorum members.
     * @param proposalHash digest of the proposal to submit
     * @return consensus result containing accepted and healthy node counts
     */
    Result requireHealthyUnanimousConsensus(String proposalHash);

    /** Consensus result containing accepted and healthy node counts.
     * @param acceptedNodes number of nodes that accepted
     * @param totalHealthyNodes number of nodes considered healthy
     */
    record Result(int acceptedNodes, int totalHealthyNodes) {
    }
}

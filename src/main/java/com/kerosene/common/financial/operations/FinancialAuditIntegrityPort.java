package com.kerosene.common.financial.operations;

import java.time.Instant;
import java.time.LocalDateTime;

/** Reads authenticated roots and proofs for the append-only financial audit log. */
public interface FinancialAuditIntegrityPort {

    /** Returns the latest signed Merkle checkpoint.
     * @return current root with signing and sequence metadata
     * @throws UnsupportedOperationException when signed roots are not supported
     */
    default AuditRoot currentRoot() {
        throw new UnsupportedOperationException("Signed audit roots are not implemented by this adapter");
    }

    /** Builds a proof that one audit sequence is included in the current tree.
     * @param sequenceNumber sequence number of the event to prove
     * @return inclusion path and the corresponding Merkle root
     * @throws UnsupportedOperationException when inclusion proofs are not supported
     */
    default InclusionProof inclusionProof(long sequenceNumber) {
        throw new UnsupportedOperationException("Audit inclusion proofs are not implemented by this adapter");
    }

    /** Builds a proof that a later checkpoint extends an earlier checkpoint consistently.
     * @param fromRoot previously trusted checkpoint
     * @param toRoot later checkpoint to compare
     * @return proof path and verification outcome
     * @throws UnsupportedOperationException when consistency proofs are not supported
     */
    default ConsistencyProof consistencyProof(AuditRoot fromRoot, AuditRoot toRoot) {
        throw new UnsupportedOperationException("Audit consistency proofs are not implemented by this adapter");
    }

    /** Signed checkpoint committing the audit log's ordered event range.
     * @param rootVersion format version of this checkpoint record
     * @param hashAlgorithm algorithm used for Merkle node digests
     * @param merkleRoot root digest of all included events
     * @param eventCount total number of events committed
     * @param fromSequence first event sequence included
     * @param toSequence last event sequence included
     * @param previousRoot preceding checkpoint digest, or null for the genesis checkpoint
     * @param generatedAt time the checkpoint was created
     * @param signerKeyId identifier of the key that signed the checkpoint
     * @param signature signature over the checkpoint's canonical contents
     * @param checkpointId unique checkpoint identifier
     */
    record AuditRoot(
        int rootVersion,          // schema version
        String hashAlgorithm,     // e.g. SHA-256
        String merkleRoot,        // hex-encoded root hash
        long eventCount,          // total events in tree
        long fromSequence,        // first sequence in this root
        long toSequence,          // last sequence in this root
        String previousRoot,      // hex-encoded previous root (null for genesis)
        Instant generatedAt,      // UTC instant
        String signerKeyId,       // who signed this root
        String signature,         // cryptographic signature over root
        String checkpointId       // unique checkpoint identifier
    ) {
        /** Validates checkpoint version, sequence range, signer, signature, and digest metadata. */
        public AuditRoot {
            if (rootVersion < 1) throw new IllegalArgumentException("rootVersion must be >= 1");
            if (hashAlgorithm == null || hashAlgorithm.isBlank()) throw new IllegalArgumentException("hashAlgorithm required");
            if (merkleRoot == null || merkleRoot.isBlank()) throw new IllegalArgumentException("merkleRoot required");
            if (eventCount < 0) throw new IllegalArgumentException("eventCount must be >= 0");
            if (fromSequence < 0) throw new IllegalArgumentException("fromSequence must be >= 0");
            if (toSequence < fromSequence) throw new IllegalArgumentException("toSequence must be >= fromSequence");
            if (generatedAt == null) throw new IllegalArgumentException("generatedAt required");
            if (signerKeyId == null || signerKeyId.isBlank()) throw new IllegalArgumentException("signerKeyId required");
            if (signature == null || signature.isBlank()) throw new IllegalArgumentException("signature required");
            if (checkpointId == null || checkpointId.isBlank()) throw new IllegalArgumentException("checkpointId required");
        }
    }

    /** Merkle path proving that one event sequence is committed by a root.
     * @param sequenceNumber event sequence whose membership is proven
     * @param merkleRoot root against which the path must be checked
     * @param proofPath ordered sibling digests from the leaf toward the root
     * @param verified whether the producer reports that it validated the path
     */
    record InclusionProof(
        long sequenceNumber,
        String merkleRoot,
        String[] proofPath,     // sibling hashes from leaf to root
        boolean verified
    ) {
        /** Requires a valid sequence, root, and non-empty sibling path. */
        public InclusionProof {
            if (sequenceNumber < 0) throw new IllegalArgumentException("sequenceNumber must be >= 0");
            if (merkleRoot == null || merkleRoot.isBlank()) throw new IllegalArgumentException("merkleRoot required");
            if (proofPath == null || proofPath.length == 0) throw new IllegalArgumentException("proofPath required");
        }
    }

    /** Merkle proof that a later audit tree extends an earlier tree without rewriting history.
     * @param fromRoot earlier trusted root digest
     * @param fromTreeSize number of events committed by the earlier root
     * @param toRoot later root digest
     * @param toTreeSize number of events committed by the later root
     * @param proofPath sibling digests used to verify append-only consistency
     * @param verified whether the producer reports that it validated the proof
     */
    record ConsistencyProof(
        String fromRoot,
        long fromTreeSize,
        String toRoot,
        long toTreeSize,
        String[] proofPath,
        boolean verified
    ) {
        /** Requires both roots and a non-empty consistency path. */
        public ConsistencyProof {
            if (fromRoot == null || fromRoot.isBlank()) throw new IllegalArgumentException("fromRoot required");
            if (toRoot == null || toRoot.isBlank()) throw new IllegalArgumentException("toRoot required");
            if (proofPath == null || proofPath.length == 0) throw new IllegalArgumentException("proofPath required");
        }
    }

}

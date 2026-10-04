package com.kerosene.common.vaultmesh.settlement;

import com.kerosene.common.vaultmesh.intent.VaultMeshReceipt;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VaultMeshSettlementTest {

    @Test
    void depositInfoValidation() {
        var info = new VaultMeshDepositInfo(
                "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                "tr(key)",
                "FROST",
                "0".repeat(64),
                "1".repeat(64),
                "MAINNET"
        );

        assertEquals("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq", info.address());
        assertEquals("tr(key)", info.descriptor());
        assertEquals("FROST", info.scheme());
        assertEquals("MAINNET", info.network());

        assertThrows(IllegalArgumentException.class, () -> new VaultMeshDepositInfo(
                null, "desc", "scheme", "pubkey", "xonly", "MAINNET"));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshDepositInfo(
                "addr", "desc", "", "pubkey", "xonly", "MAINNET"));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshDepositInfo(
                "addr", "desc", "scheme", "pubkey", "   ", "MAINNET"));
    }

    @Test
    void psbtRequestValidation() {
        var req = new VaultMeshPsbtRequest(
                "intent-1", "session-1", "USERS", "bc1q...", 50000L, "cHNidP8..."
        );
        assertEquals("intent-1", req.intentId());
        assertEquals("session-1", req.sessionId());
        assertEquals(50000L, req.amountSats());
        assertTrue(req.shouldCommitIntent());

        var reqNoCommit = new VaultMeshPsbtRequest(
                "intent-1", "session-1", "USERS", "bc1q...", 50000L, "cHNidP8...", false
        );
        assertFalse(reqNoCommit.shouldCommitIntent());
    }

    @Test
    void psbtReceiptValidation() {
        var now = Instant.now();
        var receipt = new VaultMeshPsbtReceipt(
                "intent-1", VaultMeshReceipt.Status.ACCEPTED, null, "cHNidP8...", "sig-proof", now
        );

        assertEquals("intent-1", receipt.intentId());
        assertEquals(VaultMeshReceipt.Status.ACCEPTED, receipt.status());
        assertEquals("cHNidP8...", receipt.signedPsbt());
        assertEquals(now, receipt.completedAt());

        assertThrows(IllegalArgumentException.class, () -> new VaultMeshPsbtReceipt(
                null, VaultMeshReceipt.Status.ACCEPTED, null, "psbt", "proof", now));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshPsbtReceipt(
                "intent-1", null, null, "psbt", "proof", now));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshPsbtReceipt(
                "intent-1", VaultMeshReceipt.Status.ACCEPTED, null, "psbt", "proof", null));
    }

    @Test
    void psbtResultHierarchy() {
        var now = Instant.now();
        var accepted = new VaultMeshPsbtResult.AcceptedPsbt(
                "intent-1", "prop-1", "psbt-1", "cHNidP8...", "const-1", 1L,
                new String[]{"node-1", "node-2"}, 2, "transcript-hash",
                "agg-proof", "key-1", "MAINNET", "USERS", 50000L, "bc1q...", now, now.plusSeconds(300)
        );
        assertEquals("intent-1", accepted.intentId());
        assertEquals(2, accepted.threshold());
        assertEquals(2, accepted.participantIds().length);

        var rejected = new VaultMeshPsbtResult.RejectedPsbt(
                "intent-1", "prop-1", "POLICY_VIOLATION", "Amount too high", now
        );
        assertEquals("POLICY_VIOLATION", rejected.reasonCode());

        var failStop = new VaultMeshPsbtResult.FailStopPsbt(
                "intent-1", "QUORUM_CORRUPTED", "Byzantine fault detected", now
        );
        assertEquals("QUORUM_CORRUPTED", failStop.reasonCode());

        // Invariant tests
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshPsbtResult.AcceptedPsbt(
                null, "prop-1", "psbt-1", "psbt", "const", 1L,
                new String[]{"node-1"}, 2, "trans", "proof", "key", "net", "b", 1L, "d", now, now));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshPsbtResult.AcceptedPsbt(
                "id", "prop-1", "psbt-1", "psbt", "const", 1L,
                new String[]{"node-1"}, 2, "trans", "proof", "key", "net", "b", 1L, "d", now, now));
    }
}

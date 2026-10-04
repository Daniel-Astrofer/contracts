package com.kerosene.common.vaultmesh.intent;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VaultMeshIntentTest {

    @Test
    void vaultMeshIntentValidation() {
        var now = Instant.now();
        var intent = new VaultMeshIntent(
                "intent-1", "USERS", "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                50000L, "p".repeat(64), now, "e".repeat(128), "m".repeat(128), "key-1", "key-2"
        );

        assertEquals("intent-1", intent.intentId());
        assertEquals("USERS", intent.bucket());
        assertEquals(50000L, intent.amountSats());
        assertEquals(now, intent.createdAt());

        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntent(
                null, "USERS", "dest", 50000L, "p", now, "e", "m", "k1", "k2"));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntent(
                "id", "", "dest", 50000L, "p", now, "e", "m", "k1", "k2"));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntent(
                "id", "USERS", "dest", 0L, "p", now, "e", "m", "k1", "k2"));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntent(
                "id", "USERS", "dest", -10L, "p", now, "e", "m", "k1", "k2"));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntent(
                "id", "USERS", "dest", 50000L, "p", null, "e", "m", "k1", "k2"));
    }

    @Test
    void vaultMeshIntentV2Validation() {
        var now = Instant.now();
        var expiry = now.plusSeconds(300);
        var auth = new HybridAuthorization("sig1", "sig2", "k1", "k2", false);

        var v2 = new VaultMeshIntentV2(
                2, "intent-2", "MAINNET", "BTC", "USERS",
                "bc1p...", 100000L, 500L, "policy", "const", 1L,
                "day-1", "nonce-1", now, expiry, "tx-hash", auth
        );

        assertEquals(2, v2.schemaVersion());
        assertEquals("intent-2", v2.intentId());
        assertEquals(100000L, v2.amountSats());
        assertEquals(500L, v2.maxFeeSats());
        assertTrue(v2.unsignedTransactionHash().isPresent());
        assertEquals("tx-hash", v2.unsignedTransactionHash().get());
        assertEquals(auth, v2.authorization());

        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntentV2(
                1, "intent-2", "MAINNET", "BTC", "USERS",
                "bc1p...", 100000L, 500L, "policy", "const", 1L,
                "day-1", "nonce-1", now, expiry, "tx-hash", auth));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntentV2(
                2, "intent-2", "MAINNET", "BTC", "USERS",
                "bc1p...", 0L, 500L, "policy", "const", 1L,
                "day-1", "nonce-1", now, expiry, "tx-hash", auth));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntentV2(
                2, "intent-2", "MAINNET", "BTC", "USERS",
                "bc1p...", 100000L, -1L, "policy", "const", 1L,
                "day-1", "nonce-1", now, expiry, "tx-hash", auth));
        assertThrows(IllegalArgumentException.class, () -> new VaultMeshIntentV2(
                2, "intent-2", "MAINNET", "BTC", "USERS",
                "bc1p...", 100000L, 500L, "policy", "const", 1L,
                "day-1", "nonce-1", now, now.minusSeconds(1), "tx-hash", auth));
    }

    @Test
    void vaultMeshReceiptValidation() {
        var now = Instant.now();
        var receipt = new VaultMeshReceipt("intent-1", VaultMeshReceipt.Status.ACCEPTED, null, "txid-abc", now);
        assertEquals("intent-1", receipt.intentId());
        assertEquals(VaultMeshReceipt.Status.ACCEPTED, receipt.status());
        assertEquals("txid-abc", receipt.txidOrProof());

        assertThrows(IllegalArgumentException.class, () ->
                new VaultMeshReceipt(null, VaultMeshReceipt.Status.ACCEPTED, null, "txid", now));
        assertThrows(IllegalArgumentException.class, () ->
                new VaultMeshReceipt("id", null, null, "txid", now));
        assertThrows(IllegalArgumentException.class, () ->
                new VaultMeshReceipt("id", VaultMeshReceipt.Status.ACCEPTED, null, "txid", null));
    }

    @Test
    void intentCommitMode() {
        assertNotNull(IntentCommitMode.valueOf("SIGN_ONLY"));
        assertNotNull(IntentCommitMode.valueOf("SIGN_AND_COMMIT"));
    }
}

package com.kerosene.common.financial.operations;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FinancialOperationsTest {

    @Test
    void userDirectoryLookupRequestValidation() {
        var byUser = FinancialUserDirectoryLookupRequest.byUsername("alice");
        assertEquals("alice", byUser.username());
        assertNull(byUser.userId());

        var byId = FinancialUserDirectoryLookupRequest.byUserId(42L);
        assertNull(byId.username());
        assertEquals(42L, byId.userId());

        assertThrows(IllegalArgumentException.class, () -> new FinancialUserDirectoryLookupRequest(null, null));
        assertThrows(IllegalArgumentException.class, () -> FinancialUserDirectoryLookupRequest.byUsername(null));
        assertThrows(IllegalArgumentException.class, () -> FinancialUserDirectoryLookupRequest.byUsername(""));
        assertThrows(IllegalArgumentException.class, () -> FinancialUserDirectoryLookupRequest.byUserId(null));
    }

    @Test
    void walletProvisioningRequestValidation() {
        var req = new FinancialWalletProvisioningRequest(10L, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq");
        assertEquals(10L, req.userId());
        assertEquals("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq", req.initialAddress());

        var reqNoAddr = new FinancialWalletProvisioningRequest(10L, null);
        assertNull(reqNoAddr.initialAddress());

        assertThrows(IllegalArgumentException.class, () -> new FinancialWalletProvisioningRequest(null, "bc1q"));
        assertThrows(IllegalArgumentException.class, () -> new FinancialWalletProvisioningRequest(10L, "  "));
    }

    @Test
    void mpcWalletKeyRequestValidation() {
        var walletId = UUID.randomUUID();
        var req = new MpcWalletKeyRequest(walletId, 100L, "FROST", "MAINNET", "INBOUND");
        assertEquals(walletId, req.walletId());
        assertEquals(100L, req.userId());
        assertEquals("FROST", req.scheme());
        assertEquals("MAINNET", req.network());
        assertEquals("INBOUND", req.purpose());

        assertThrows(IllegalArgumentException.class, () -> new MpcWalletKeyRequest(null, 100L, "FROST", "MAINNET", "INBOUND"));
        assertThrows(IllegalArgumentException.class, () -> new MpcWalletKeyRequest(walletId, null, "FROST", "MAINNET", "INBOUND"));
        assertThrows(IllegalArgumentException.class, () -> new MpcWalletKeyRequest(walletId, 100L, "", "MAINNET", "INBOUND"));
    }

    @Test
    void mpcWalletKeyReceiptValidation() {
        var walletId = UUID.randomUUID();
        var now = Instant.now();
        var receipt = new MpcWalletKeyReceipt(
                "key-1", walletId, "FROST", "MAINNET", "0".repeat(64),
                "tr(key)", "bc1p...", "c".repeat(64), now, "attestation-token"
        );

        assertEquals("key-1", receipt.keyId());
        assertEquals(walletId, receipt.walletId());
        assertEquals("FROST", receipt.scheme());
        assertEquals(now, receipt.createdAt());

        assertThrows(IllegalArgumentException.class, () -> new MpcWalletKeyReceipt(
                null, walletId, "FROST", "MAINNET", "0".repeat(64),
                "tr(key)", "bc1p...", "c".repeat(64), now, "attestation-token"
        ));
    }
}

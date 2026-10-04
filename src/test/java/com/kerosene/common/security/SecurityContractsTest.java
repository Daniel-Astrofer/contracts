package com.kerosene.common.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContractsTest {

    @Test
    void cryptoPurposeValues() {
        assertNotNull(CryptoPurpose.valueOf("COLUMN_ENCRYPTION"));
        assertNotNull(CryptoPurpose.valueOf("WALLET_SECRET"));
        assertNotNull(CryptoPurpose.valueOf("API_KEY"));
        assertNotNull(CryptoPurpose.valueOf("TOKEN"));
    }

    @Test
    void encryptedValueValidation() {
        var now = Instant.now();
        var enc = new EncryptedValue(
                "kms-key-1",
                "AES-256-GCM",
                1,
                new byte[]{1, 2, 3},
                new byte[]{4, 5, 6},
                new byte[]{7, 8, 9},
                now
        );

        assertEquals("kms-key-1", enc.keyId());
        assertEquals("AES-256-GCM", enc.algorithm());
        assertEquals(1, enc.version());
        assertArrayEquals(new byte[]{1, 2, 3}, enc.nonce());
        assertArrayEquals(new byte[]{4, 5, 6}, enc.ciphertext());
        assertArrayEquals(new byte[]{7, 8, 9}, enc.authenticationTag());
        assertEquals(now, enc.createdAt());

        assertThrows(IllegalArgumentException.class, () -> new EncryptedValue(
                null, "AES-256-GCM", 1, new byte[]{1}, new byte[]{1}, new byte[]{1}, now));
        assertThrows(IllegalArgumentException.class, () -> new EncryptedValue(
                "key", null, 1, new byte[]{1}, new byte[]{1}, new byte[]{1}, now));
        assertThrows(IllegalArgumentException.class, () -> new EncryptedValue(
                "key", "AES", 1, new byte[0], new byte[]{1}, new byte[]{1}, now));
        assertThrows(IllegalArgumentException.class, () -> new EncryptedValue(
                "key", "AES", 1, new byte[]{1}, new byte[0], new byte[]{1}, now));
        assertThrows(IllegalArgumentException.class, () -> new EncryptedValue(
                "key", "AES", 1, new byte[]{1}, new byte[]{1}, new byte[0], now));
        assertThrows(IllegalArgumentException.class, () -> new EncryptedValue(
                "key", "AES", 1, new byte[]{1}, new byte[]{1}, new byte[]{1}, null));
    }
}

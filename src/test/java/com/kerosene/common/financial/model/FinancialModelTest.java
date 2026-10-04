package com.kerosene.common.financial.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FinancialModelTest {

    @Test
    void bitcoinAddressValidation() {
        // Valid Bech32
        var addr1 = new BitcoinAddress("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq");
        assertEquals("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq", addr1.value());
        assertEquals("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq", addr1.toString());

        // Valid Bech32m (Taproot)
        var addr2 = new BitcoinAddress("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0");
        assertEquals("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0", addr2.value());

        // Valid Base58
        var addr3 = new BitcoinAddress("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2");
        assertEquals("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", addr3.value());

        // Testnet addresses
        var addr4 = new BitcoinAddress("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx");
        assertEquals("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx", addr4.value());

        // Invalid
        assertThrows(IllegalArgumentException.class, () -> new BitcoinAddress(null));
        assertThrows(IllegalArgumentException.class, () -> new BitcoinAddress(""));
        assertThrows(IllegalArgumentException.class, () -> new BitcoinAddress("not-an-address"));
    }

    @Test
    void satoshiAmountValidation() {
        var zero = new SatoshiAmount(0);
        assertEquals(0, zero.sats());
        assertEquals("0", zero.toString());

        var amount = new SatoshiAmount(21_000_000_00000000L);
        assertEquals(21_000_000_00000000L, amount.sats());
        assertEquals("2100000000000000", amount.toString());

        assertThrows(IllegalArgumentException.class, () -> new SatoshiAmount(-1));
    }

    @Test
    void dayEpochValidation() {
        var epoch = new DayEpoch("2026-10-04");
        assertEquals("2026-10-04", epoch.value());
        assertEquals("2026-10-04", epoch.toString());

        assertThrows(IllegalArgumentException.class, () -> new DayEpoch(null));
        assertThrows(IllegalArgumentException.class, () -> new DayEpoch("  "));
    }

    @Test
    void nonceValidation() {
        byte[] bytes = new byte[16];
        Arrays.fill(bytes, (byte) 0x42);
        var nonce = new Nonce(bytes);
        assertArrayEquals(bytes, nonce.value());
        assertEquals("Nonce[len=16]", nonce.toString());

        assertThrows(IllegalArgumentException.class, () -> new Nonce(null));
        assertThrows(IllegalArgumentException.class, () -> new Nonce(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> new Nonce(new byte[11]));
        assertThrows(IllegalArgumentException.class, () -> new Nonce(new byte[33]));
    }

    @Test
    void hashesValidation() {
        var hashHex = "a".repeat(64);

        var constitution = new ConstitutionHash(hashHex);
        assertEquals(hashHex, constitution.value());
        assertEquals(hashHex, constitution.toString());
        assertThrows(IllegalArgumentException.class, () -> new ConstitutionHash(null));
        assertThrows(IllegalArgumentException.class, () -> new ConstitutionHash(""));

        var paymentHash = new PaymentHash(hashHex);
        assertEquals(hashHex, paymentHash.value());
        assertEquals(hashHex, paymentHash.toString());
        assertThrows(IllegalArgumentException.class, () -> new PaymentHash(null));
        assertThrows(IllegalArgumentException.class, () -> new PaymentHash("short"));

        var policyHash = new PolicyHash(hashHex);
        assertEquals(hashHex, policyHash.value());
        assertEquals(hashHex, policyHash.toString());
        assertThrows(IllegalArgumentException.class, () -> new PolicyHash(null));

        var proposalHash = new ProposalHash(hashHex);
        assertEquals(hashHex, proposalHash.value());
        assertEquals(hashHex, proposalHash.toString());
        assertThrows(IllegalArgumentException.class, () -> new ProposalHash(null));
    }

    @Test
    void providerIdAndReasonCode() {
        var provider = new ProviderId("lnd-01");
        assertEquals("lnd-01", provider.value());
        assertEquals("lnd-01", provider.toString());
        assertThrows(IllegalArgumentException.class, () -> new ProviderId(null));
        assertThrows(IllegalArgumentException.class, () -> new ProviderId(""));

        var reason = new ReasonCode("INSUFFICIENT_FUNDS");
        assertEquals("INSUFFICIENT_FUNDS", reason.value());
        assertEquals("INSUFFICIENT_FUNDS", reason.toString());
        assertThrows(IllegalArgumentException.class, () -> new ReasonCode(null));
    }

    @Test
    void psbtBase64Validation() {
        var psbt = new PsbtBase64("cHNidP8BAFICAAAA");
        assertEquals("cHNidP8BAFICAAAA", psbt.value());
        assertEquals("cHNidP8BAFICAAAA", psbt.toString());

        assertThrows(IllegalArgumentException.class, () -> new PsbtBase64(null));
        assertThrows(IllegalArgumentException.class, () -> new PsbtBase64("   "));
    }

    @Test
    void xOnlyPublicKeyValidation() {
        var keyHex = "0".repeat(64);
        var pubkey = new XOnlyPublicKey(keyHex);
        assertEquals(keyHex, pubkey.value());
        assertEquals(keyHex, pubkey.toString());

        assertThrows(IllegalArgumentException.class, () -> new XOnlyPublicKey(null));
        assertThrows(IllegalArgumentException.class, () -> new XOnlyPublicKey("short"));
    }

    @Test
    void networkRailBucket() {
        assertEquals("MAINNET", new BitcoinNetwork("MAINNET").value());
        assertEquals("LIGHTNING", new Rail("LIGHTNING").value());
        assertEquals("USERS", new VaultBucket("USERS").value());
    }
}

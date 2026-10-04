package io.kerosene.contracts.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscoveryContractsJsonTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Test
    void peerHelloV1SerializationRoundtrip() throws Exception {
        var hello = new PeerHelloV1(
                "0.2.0",
                "kerosene-testnet",
                DiscoveryPlane.BANK,
                "a".repeat(64),
                "b".repeat(64),
                "c".repeat(64),
                1700000000000L,
                "https://abcdefghijklmnopqrstuvwxyz234567abcdefghijklmnopqrstuvwx.onion",
                "d".repeat(128)
        );

        String json = mapper.writeValueAsString(hello);
        assertTrue(json.contains("\"contract_version\""));
        assertTrue(json.contains("\"network_id\""));
        assertTrue(json.contains("\"plane\":\"bank\""));
        assertTrue(json.contains("\"member_id\""));

        var deserialized = mapper.readValue(json, PeerHelloV1.class);
        assertEquals(hello, deserialized);
    }

    @Test
    void peerHelloV1RejectsInvalidFields() {
        assertThrows(IllegalArgumentException.class, () ->
                new PeerHelloV1(null, "net", DiscoveryPlane.BANK, "m", "r", "c", 1, "e", "s"));
        assertThrows(IllegalArgumentException.class, () ->
                new PeerHelloV1("0.2.0", "", DiscoveryPlane.BANK, "m", "r", "c", 1, "e", "s"));
        assertThrows(IllegalArgumentException.class, () ->
                new PeerHelloV1("0.2.0", "net", null, "m", "r", "c", 1, "e", "s"));
        assertThrows(IllegalArgumentException.class, () ->
                new PeerHelloV1("0.2.0", "net", DiscoveryPlane.BANK, "m", "r", "c", -1, "e", "s"));
    }

    @Test
    void genesisTrustBundleV1SerializationRoundtrip() throws Exception {
        var bankMember = new TrustMember("a".repeat(64), "b".repeat(64));
        var vaultMember = new TrustMember("c".repeat(64), "d".repeat(64));
        var bankPlane = new TrustPlane(1, List.of(bankMember));
        var vaultPlane = new TrustPlane(2, List.of(vaultMember));

        var bundle = new GenesisTrustBundleV1(
                "0.2.0",
                "kerosene-mainnet",
                bankPlane,
                vaultPlane,
                1700000000000L
        );

        String json = mapper.writeValueAsString(bundle);
        assertTrue(json.contains("\"contract_version\""));
        assertTrue(json.contains("\"bank\""));
        assertTrue(json.contains("\"vault\""));

        var deserialized = mapper.readValue(json, GenesisTrustBundleV1.class);
        assertEquals(bundle, deserialized);
    }

    @Test
    void trustPlaneRejectsInvalidThreshold() {
        var member = new TrustMember("a".repeat(64), "b".repeat(64));
        assertThrows(IllegalArgumentException.class, () -> new TrustPlane(0, List.of(member)));
        assertThrows(IllegalArgumentException.class, () -> new TrustPlane(65536, List.of(member)));
        assertThrows(IllegalArgumentException.class, () -> new TrustPlane(1, List.of()));
    }

    @Test
    void membershipManifestV1SerializationRoundtrip() throws Exception {
        var member = new ManifestMember(
                "a".repeat(64),
                "b".repeat(64),
                "https://abcdefghijklmnopqrstuvwxyz234567abcdefghijklmnopqrstuvwx.onion"
        );
        var sig = new ManifestSignature("c".repeat(64), "d".repeat(128));

        var manifest = new MembershipManifestV1(
                "0.2.0",
                "kerosene-mainnet",
                DiscoveryPlane.VAULT,
                5,
                MembershipPhase.STABLE,
                "0".repeat(64),
                2,
                List.of(member),
                null,
                List.of(sig)
        );

        String json = mapper.writeValueAsString(manifest);
        assertTrue(json.contains("\"epoch\":5"));
        assertTrue(json.contains("\"phase\":\"stable\""));

        var deserialized = mapper.readValue(json, MembershipManifestV1.class);
        assertEquals(manifest, deserialized);
    }

    @Test
    void admissionRequestV1SerializationRoundtrip() throws Exception {
        var candidate = new ManifestMember(
                "a".repeat(64),
                "b".repeat(64),
                "https://abcdefghijklmnopqrstuvwxyz234567abcdefghijklmnopqrstuvwx.onion"
        );
        var request = new AdmissionRequestV1(
                "0.2.0",
                "kerosene-testnet",
                DiscoveryPlane.BANK,
                candidate,
                "s".repeat(64),
                "c".repeat(64),
                1700000000000L,
                "f".repeat(128)
        );

        String json = mapper.writeValueAsString(request);
        assertTrue(json.contains("\"candidate\""));
        assertTrue(json.contains("\"sponsor_id\""));

        var deserialized = mapper.readValue(json, AdmissionRequestV1.class);
        assertEquals(request, deserialized);
    }
}

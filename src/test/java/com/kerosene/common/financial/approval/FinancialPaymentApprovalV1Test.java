package com.kerosene.common.financial.approval;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static com.kerosene.common.financial.approval.FinancialPaymentApprovalV1.*;

class FinancialPaymentApprovalV1Test {
    private final ObjectMapper json = new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private JsonNode vector() throws Exception {
        try (var is = getClass().getResourceAsStream("/financial-payment-approval-v1.json")) {
            if (is != null) {
                return json.readTree(is);
            }
        }
        var p = Path.of("test-vectors/financial-payment-approval-v1.json");
        if (!Files.exists(p)) {
            p = Path.of("../test-vectors/financial-payment-approval-v1.json");
        }
        return json.readTree(Files.readString(p));
    }
    private Context context() throws Exception { return json.treeToValue(vector().get("context"), Context.class); }
    private Challenge challenge() throws Exception { return json.treeToValue(vector().get("challenge"), Challenge.class); }

    @Test void bindingAndPayloadMatchSharedUnicodeVectorExactly() throws Exception {
        var v = vector();
        assertEquals(v.get("bindingHash").asText(), bindingHash(context()));
        assertEquals(v.get("signedPayload").asText(), signedPayload(challenge(), v.get("credentialId").asText(),
                v.get("deviceInstallId").asText(), v.get("counter").asLong(), v.get("issuedAtEpochSeconds").asLong()));
    }

    @Test void sharedVectorHasARealEd25519SignatureAndRejectsTampering() throws Exception {
        var v = vector();
        byte[] raw = Base64.getUrlDecoder().decode(v.get("publicKey").asText());
        byte[] prefix = HexFormat.of().parseHex("302a300506032b6570032100");
        byte[] encoded = new byte[prefix.length + raw.length];
        System.arraycopy(prefix, 0, encoded, 0, prefix.length); System.arraycopy(raw, 0, encoded, prefix.length, raw.length);
        var key = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(encoded));
        var verifier = Signature.getInstance("Ed25519");
        byte[] signature = Base64.getUrlDecoder().decode(v.get("signature").asText());
        verifier.initVerify(key); verifier.update(v.get("signedPayload").asText().getBytes(StandardCharsets.UTF_8));
        assertTrue(verifier.verify(signature));
        verifier.initVerify(key); verifier.update((v.get("signedPayload").asText() + " ").getBytes(StandardCharsets.UTF_8));
        assertFalse(verifier.verify(signature));
    }

    @Test void everyContextFieldParticipatesAndNullIsNotEmpty() throws Exception {
        var base = (ObjectNode) vector().get("context");
        var fields = base.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next(); var changed = base.deepCopy(); var current = changed.get(field);
            switch (field) {
                case "rail" -> changed.put(field, "LIGHTNING");
                case "direction" -> { changed.put("rail", "INTERNAL"); changed.put(field, "INTERNAL"); }
                default -> { if (current.isIntegralNumber()) { changed.put(field, current.asLong() + 1); }
                    else { changed.put(field, current.isNull() ? "" : current.asText() + "x"); } }
            }
            assertNotEquals(bindingHash(context()), bindingHash(json.treeToValue(changed, Context.class)), field);
        }
    }

    @Test void inputVersionsProofTypesUnknownFieldsAndDuplicatesFailClosed() throws Exception {
        var proof = json.createObjectNode().put("version", 1).put("type", PROOF_TYPE)
                .put("credentialId", "credential").put("deviceInstallId", "install")
                .put("signedPayload", "payload").put("signature", "signature");
        assertNotNull(json.treeToValue(proof, Proof.class));
        assertThrows(Exception.class, () -> json.treeToValue(proof.deepCopy().put("version", 2), Proof.class));
        assertThrows(Exception.class, () -> json.treeToValue(proof.deepCopy().put("type", "DEVICE_KEY"), Proof.class));
        assertThrows(Exception.class, () -> json.treeToValue(proof.deepCopy().put("username", "ignored?"), Proof.class));
        assertThrows(Exception.class, () -> json.readTree("{\"version\":1,\"version\":2}"));
        assertThrows(IllegalArgumentException.class, () -> new Request(2, context(), null, null, null, null));
    }

    @Test void invalidRoutesAmountsUnicodeAndUnsafeIntegersAreRejected() throws Exception {
        var base = (ObjectNode) vector().get("context");
        assertThrows(Exception.class, () -> json.treeToValue(base.deepCopy().put("direction", "INBOUND"), Context.class));
        assertThrows(Exception.class, () -> json.treeToValue(base.deepCopy().put("amountSats", 0), Context.class));
        assertThrows(Exception.class, () -> json.treeToValue(base.deepCopy().put("networkFeeSats", -1), Context.class));
        assertThrows(Exception.class, () -> json.treeToValue(base.deepCopy().put("userId", 9007199254740992L), Context.class));
        assertThrows(Exception.class, () -> json.treeToValue(base.deepCopy().put("memo", "\uD800"), Context.class));
        assertThrows(Exception.class, () -> json.treeToValue(base.deepCopy().put("memo", "\uDC00"), Context.class));
        assertNotNull(json.treeToValue(base.deepCopy().put("memo", "\uD83D\uDE80"), Context.class));
    }

    @Test void challengeIntervalCountersAndSigningTimeAreBounded() throws Exception {
        var c = challenge(); var node = (ObjectNode) vector().get("challenge");
        assertThrows(Exception.class, () -> json.treeToValue(node.deepCopy().put("expiresAtEpochSeconds", c.issuedAtEpochSeconds()+121), Challenge.class));
        assertThrows(Exception.class, () -> json.treeToValue(node.deepCopy().put("purpose", "AUTH_DEVICE_KEY"), Challenge.class));
        assertThrows(Exception.class, () -> json.treeToValue(node.deepCopy().put("algorithm", "none"), Challenge.class));
        assertThrows(IllegalArgumentException.class, () -> signedPayload(c, "cred", "install", 0, c.issuedAtEpochSeconds()));
        assertThrows(IllegalArgumentException.class, () -> signedPayload(c, "cred", "install", 9007199254740992L, c.issuedAtEpochSeconds()));
        assertThrows(IllegalArgumentException.class, () -> signedPayload(c, "cred", "install", 1, c.expiresAtEpochSeconds()));
        assertThrows(IllegalArgumentException.class, () -> signedPayload(c, "cred", "install", 1, c.issuedAtEpochSeconds()-1));
    }

    @Test void responseStatesAreExclusiveAndDiagnosticsNeverPrintMaterial() throws Exception {
        var c = challenge();
        var challengeResponse = new Response(1, "CHALLENGE", c.bindingHash(), null, c.expiresAtEpochSeconds(), c);
        var approved = new Response(1, "APPROVED", c.bindingHash(), c.challengeId(), c.expiresAtEpochSeconds(), null);
        assertEquals(challengeResponse, json.readValue(json.writeValueAsString(challengeResponse), Response.class));
        assertEquals(approved, json.readValue(json.writeValueAsString(approved), Response.class));
        assertThrows(IllegalArgumentException.class, () -> new Response(1, "APPROVED", c.bindingHash(), null, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new Response(1, "CHALLENGE", c.bindingHash(), "id", c.expiresAtEpochSeconds(), c));
        assertThrows(IllegalArgumentException.class, () -> new Response(1, "CHALLENGE", "b".repeat(64), null, c.expiresAtEpochSeconds(), c));
        assertThrows(IllegalArgumentException.class, () -> new Response(1, "UNKNOWN", c.bindingHash(), "id", 1, null));
        var proof = new Proof(1, PROOF_TYPE, "cred-secret", "device-secret", "payload-secret", "signature-secret");
        var req = new Request(1, context(), "pin-secret", "totp-secret", "phrase-secret", proof);
        for (var value : new Object[]{context(), c, proof, req, challengeResponse, approved}) {
            for (String secret : new String[]{c.bindingHash(), c.challenge(), c.username(), "pin-secret", "payload-secret", "signature-secret"}) {
                assertFalse(value.toString().contains(secret));
            }
        }
    }

    @Test void timestampsAndSchemaIntegerBoundsAgreeAcrossConsumers() throws Exception {
        var node = (ObjectNode) vector().get("challenge");
        assertThrows(Exception.class, () -> json.treeToValue(node.deepCopy()
                .put("issuedAtEpochSeconds", 9_007_199_254_740_991L)
                .put("expiresAtEpochSeconds", 9_007_199_254_740_992L), Challenge.class));
        assertNotNull(json.treeToValue(node.deepCopy()
                .put("issuedAtEpochSeconds", 9_007_199_254_740_990L)
                .put("expiresAtEpochSeconds", 9_007_199_254_740_991L), Challenge.class));
        assertThrows(IllegalArgumentException.class, () -> new Response(1, "APPROVED", challenge().bindingHash(),
                "id", 9_007_199_254_740_992L, null));
        var properties = json.readTree(Files.readString(Path.of("schemas/financial/payment-approval-v1.schema.json")))
                .path("$defs").path("context").path("properties");
        assertEquals(9_007_199_254_740_991L, properties.path("feeRateSatPerVbyte").path("maximum").longValue());
        assertEquals(-9_007_199_254_740_991L, properties.path("feeRateSatPerVbyte").path("minimum").longValue());
        assertEquals(Integer.MAX_VALUE, properties.path("feeTargetBlocks").path("maximum").intValue());
        assertEquals(Integer.MIN_VALUE, properties.path("feeTargetBlocks").path("minimum").intValue());
    }

    @Test void normativeSchemaFieldsAgreeWithJavaRecords() throws Exception {
        var schema = json.readTree(Files.readString(Path.of("schemas/financial/payment-approval-v1.schema.json"))).get("$defs");
        for (var type : new Class<?>[]{Context.class, Proof.class, Request.class, Challenge.class, Response.class}) {
            String name = type.getSimpleName().toLowerCase(); Set<String> expected = new HashSet<>();
            schema.get(name).get("properties").fieldNames().forEachRemaining(expected::add);
            Set<String> actual = new HashSet<>(); for (var part : type.getRecordComponents()) { actual.add(part.getName()); }
            assertEquals(expected, actual, name);
            assertFalse(schema.get(name).get("additionalProperties").asBoolean());
        }
    }

    @Test void canonicalEscapingMatchesJsonForControlsQuotesBackslashesAndUnicode() throws Exception {
        var c = challenge();
        for (String name : new String[]{"a\"b\\c", "a\b\f\n\r\t\u0001z", "漢 á \uD83D\uDE80"}) {
            var variant = new Challenge(1, PURPOSE, c.challengeId(), c.challenge(), c.bindingHash(), name,
                    c.onionServiceId(), c.issuedAtEpochSeconds(), c.expiresAtEpochSeconds(), c.algorithm(), c.canonicalization());
            String payload = signedPayload(variant, "cred", "install", 1, c.issuedAtEpochSeconds());
            assertEquals(name, json.readTree(payload).get("username").textValue());
            var ordered = json.readValue(payload, new com.fasterxml.jackson.core.type.TypeReference<java.util.TreeMap<String, Object>>() {});
            assertEquals(json.writeValueAsString(ordered), payload);
        }
    }
}

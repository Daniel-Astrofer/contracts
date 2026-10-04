package io.kerosene.contracts.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.TreeMap;

/**
 * Canonical JSON utility for cross-language deterministic JSON serialization.
 * <p>
 * Canonical JSON guarantees:
 * <ol>
 *   <li>Keys are sorted lexicographically at every nesting level.
 *   <li>Output is compact (no whitespace).
 *   <li>Integers are serialized as bare numbers.
 * </ol>
 * These properties are verified by KAT test vectors shared between Java and
 * Rust so that both languages produce identical bytes for the same struct.
 */
public final class CanonicalJson {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    /** Prevents construction of this deterministic serialization utility. */
    private CanonicalJson() {}

    /**
     * Serialize an object to canonical JSON bytes.
     * Keys are sorted recursively, output is compact.
     * @param value contract object to convert through Jackson
     * @return UTF-8 bytes of compact JSON with recursively sorted object keys
     * @throws RuntimeException if the value cannot be represented as JSON
     */
    public static byte[] toBytes(Object value) {
        try {
            JsonNode tree = MAPPER.valueToTree(value);
            JsonNode sorted = sortNode(tree);
            return sorted.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Cannot serialize to canonical JSON", e);
        }
    }

    /**
     * SHA-256 hash of the canonical JSON bytes.
     * @param value contract object whose canonical representation is hashed
     * @return lowercase hexadecimal SHA-256 digest
     * @throws RuntimeException if serialization fails or SHA-256 is unavailable
     */
    public static String hash(Object value) {
        byte[] bytes = toBytes(value);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Recursively sort all JSON object keys using TreeMap for lexicographic order.
     */
    /** Recursively rebuilds objects with lexicographically sorted keys, preserving array order.
     * @param node JSON subtree to canonicalize
     * @return canonicalized subtree; scalar nodes are returned unchanged
     */
    private static JsonNode sortNode(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            TreeMap<String, JsonNode> sorted = new TreeMap<>();
            obj.fieldNames().forEachRemaining(k -> sorted.put(k, sortNode(obj.get(k))));
            ObjectNode result = MAPPER.createObjectNode();
            sorted.forEach(result::set);
            return result;
        }
        if (node instanceof ArrayNode arr) {
            ArrayNode result = MAPPER.createArrayNode();
            arr.forEach(e -> result.add(sortNode(e)));
            return result;
        }
        return node;
    }
}

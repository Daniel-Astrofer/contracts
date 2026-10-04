package com.kerosene.common.financial.stomp;

/**
 * Typed STOMP event body — immutable, size-limited, and schema-versioned.
 * {@code bodyLength} records the serialized body's byte count and is capped at 65,536.
 * @param eventId stable identifier used for delivery correlation and deduplication
 * @param schemaVersion payload schema revision used by the consumer
 * @param eventType event discriminator interpreted by the subscribing client
 * @param serializedBody already serialized event body; limited to 65,536 bytes
 * @param bodyLength serialized body byte count supplied by the producer
 */
public record StompPayload(
    String eventId,
    int schemaVersion,
    String eventType,
    String serializedBody,     // already serialized, max 65536 bytes
    int bodyLength
) {
    /** Maximum permitted serialized event body size, in bytes. */
    public static final int MAX_PAYLOAD_BYTES = 65536;

    /** Enforces identifiers, serialized body presence, and the maximum declared payload size.
     * @param eventId event identifier
     * @param schemaVersion consumer schema revision
     * @param eventType event discriminator
     * @param serializedBody serialized event content
     * @param bodyLength serialized body size in bytes
     * @throws IllegalArgumentException if required text is missing or bodyLength is out of range
     */
    public StompPayload {
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId required");
        if (eventType == null || eventType.isBlank()) throw new IllegalArgumentException("eventType required");
        if (serializedBody == null) throw new IllegalArgumentException("serializedBody required");
        if (bodyLength < 0 || bodyLength > MAX_PAYLOAD_BYTES) throw new IllegalArgumentException("bodyLength out of range 0-" + MAX_PAYLOAD_BYTES);
    }
}

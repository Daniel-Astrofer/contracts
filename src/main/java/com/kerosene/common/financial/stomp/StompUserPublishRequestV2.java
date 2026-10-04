package com.kerosene.common.financial.stomp;

import java.util.Set;

/**
 * V2 user-scoped publish request using a typed, allowlisted destination and payload.
 * @param userId recipient's platform user identifier represented as text
 * @param destination queue enum restricted to supported user destinations
 * @param payload typed schema-versioned STOMP event envelope
 */
public record StompUserPublishRequestV2(
    String userId,                       // non-blank
    StompDestination destination,        // enum, not arbitrary string
    StompPayload payload                 // typed, not Map<String,Object>
) {
    /** Wire paths accepted by the versioned user-publish adapter. */
    public static final Set<String> ALLOWED_DESTINATIONS = Set.of(
        "/queue/balance",
        "/queue/transaction",
        "/queue/notification"
    );

    /** Requires recipient, typed destination, and typed payload.
     * @param userId recipient identifier
     * @param destination allowlisted user queue
     * @param payload event envelope
     * @throws IllegalArgumentException if any component is absent
     */
    public StompUserPublishRequestV2 {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId required");
        if (destination == null) throw new IllegalArgumentException("destination required");
        if (payload == null) throw new IllegalArgumentException("payload required");
    }
}

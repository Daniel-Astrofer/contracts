package com.kerosene.common.financial.stomp;

import java.util.Map;

/**
 * KFE -> Core relay: publish a user-scoped STOMP frame on the auth/server broker.
 *
 * <p>{@code destination} must be a relative user queue (e.g. {@code /queue/balance}),
 * not a {@code /user/...} path - {@code convertAndSendToUser} adds the user prefix.
 * @param userId authenticated recipient's platform user identifier
 * @param destination relative user queue destination, without a {@code /user/} prefix
 * @param payload event fields to serialize and send to that user's broker queue
 */
public record StompUserPublishRequest(
        Long userId,
        String destination,
        Map<String, Object> payload) {

    /** Requires recipient, relative destination, and payload.
     * @param userId recipient user identifier
     * @param destination relative queue path
     * @param payload event body fields
     * @throws IllegalArgumentException if any required value is absent
     */
    public StompUserPublishRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("destination required");
        if (payload == null) throw new IllegalArgumentException("payload required");
    }
}

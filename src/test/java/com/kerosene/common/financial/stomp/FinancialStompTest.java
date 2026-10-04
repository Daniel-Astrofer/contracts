package com.kerosene.common.financial.stomp;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinancialStompTest {

    @Test
    void stompDestinationPaths() {
        assertEquals("/queue/balance", StompDestination.QUEUE_BALANCE.path());
        assertEquals("/queue/transaction", StompDestination.QUEUE_TRANSACTION.path());
        assertEquals("/queue/notification", StompDestination.QUEUE_NOTIFICATION.path());

        assertEquals(StompDestination.QUEUE_BALANCE, StompDestination.fromPath("/queue/balance"));
        assertEquals(StompDestination.QUEUE_TRANSACTION, StompDestination.fromPath("/queue/transaction"));
        assertEquals(StompDestination.QUEUE_NOTIFICATION, StompDestination.fromPath("/queue/notification"));

        assertThrows(IllegalArgumentException.class, () -> StompDestination.fromPath("/unknown"));
    }

    @Test
    void stompPayloadValidation() {
        var payload = new StompPayload("evt-1", 1, "BALANCE_UPDATE", "{\"sats\":100}", 14);
        assertEquals("evt-1", payload.eventId());
        assertEquals(1, payload.schemaVersion());
        assertEquals("BALANCE_UPDATE", payload.eventType());
        assertEquals("{\"sats\":100}", payload.serializedBody());
        assertEquals(14, payload.bodyLength());

        assertThrows(IllegalArgumentException.class, () -> new StompPayload(null, 1, "TYPE", "{}", 2));
        assertThrows(IllegalArgumentException.class, () -> new StompPayload("evt", 1, null, "{}", 2));
        assertThrows(IllegalArgumentException.class, () -> new StompPayload("evt", 1, "TYPE", null, 2));
        assertThrows(IllegalArgumentException.class, () -> new StompPayload("evt", 1, "TYPE", "{}", -1));
        assertThrows(IllegalArgumentException.class, () -> new StompPayload("evt", 1, "TYPE", "{}", StompPayload.MAX_PAYLOAD_BYTES + 1));
    }

    @Test
    void stompUserPublishRequestValidation() {
        var req = new StompUserPublishRequest(100L, "/queue/balance", Map.of("balance", 5000L));
        assertEquals(100L, req.userId());
        assertEquals("/queue/balance", req.destination());
        assertEquals(5000L, req.payload().get("balance"));

        assertThrows(IllegalArgumentException.class, () -> new StompUserPublishRequest(null, "/queue/balance", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new StompUserPublishRequest(100L, "", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new StompUserPublishRequest(100L, "/queue/balance", null));
    }

    @Test
    void stompUserPublishRequestV2Validation() {
        var payload = new StompPayload("evt-2", 2, "TX_CONFIRMED", "{\"tx\":1}", 8);
        var req = new StompUserPublishRequestV2("user-100", StompDestination.QUEUE_TRANSACTION, payload);
        assertEquals("user-100", req.userId());
        assertEquals(StompDestination.QUEUE_TRANSACTION, req.destination());
        assertEquals(payload, req.payload());

        assertThrows(IllegalArgumentException.class, () -> new StompUserPublishRequestV2(null, StompDestination.QUEUE_TRANSACTION, payload));
        assertThrows(IllegalArgumentException.class, () -> new StompUserPublishRequestV2("user-100", null, payload));
        assertThrows(IllegalArgumentException.class, () -> new StompUserPublishRequestV2("user-100", StompDestination.QUEUE_TRANSACTION, null));
    }
}

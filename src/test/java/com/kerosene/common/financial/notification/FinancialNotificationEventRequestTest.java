package com.kerosene.common.financial.notification;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FinancialNotificationEventRequestTest {
    private FinancialNotificationEventRequest request(int version, long user, long amount, int confirmations, long sequence) {
        return new FinancialNotificationEventRequest(UUID.randomUUID(), version,
                FinancialNotificationEventRequest.Type.PAYMENT_CONFIRMED, user, UUID.randomUUID(), UUID.randomUUID(),
                "ONCHAIN", amount, confirmations, null, null, null, null, Instant.now(), sequence);
    }
    @Test void rejectsUnsupportedVersionAndInvalidFinancialBounds() {
        assertThrows(IllegalArgumentException.class, () -> request(2, 42, 1500, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> request(1, 0, 1500, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> request(1, 42, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> request(1, 42, 1500, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> request(1, 42, 1500, 1, -1));
    }
    @Test void anUnsupportedAdapterCannotSilentlyAcknowledgeAnEvent() {
        FinancialNotificationPort unsupported = new FinancialNotificationPort() {};
        assertThrows(UnsupportedOperationException.class, () -> unsupported.deliverEvent(request(1, 42, 1500, 1, 1)));
    }
}

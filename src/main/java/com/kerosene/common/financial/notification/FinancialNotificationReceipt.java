package com.kerosene.common.financial.notification;

import java.util.UUID;

/** Acceptance acknowledges durable inbox persistence, not delivery to an OS device. */
public record FinancialNotificationReceipt(UUID eventId, boolean accepted) {
    public FinancialNotificationReceipt {
        if (eventId == null) throw new IllegalArgumentException("Event identity is required.");
    }
}

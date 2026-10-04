package com.kerosene.common.financial.notification;

import java.util.Map;

/** Records redacted audit metadata for notification-token lifecycle events. */
public interface FinancialNotificationAuditPort {

    /** Persists a device-token event with already-redacted context.
     * @param eventType stable token lifecycle event name
     * @param redactedPayload metadata with secrets and token values removed
     */
    void recordDeviceTokenEvent(String eventType, Map<String, ?> redactedPayload);
}

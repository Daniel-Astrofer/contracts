package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/** Correlation reference to a persisted audit event in an administrative response.
 * @param eventId stable audit event identifier
 * @param requestId request associated with the audited operation
 * @param occurredAt event occurrence timestamp in the contract's wire format
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AuditReferenceV1(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("occurred_at") String occurredAt) {}

package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

/** Versioned administrative API error envelope serialized with snake_case property names.
 * @param contractVersion schema version for this response
 * @param code stable machine-readable error identifier
 * @param message safe human-readable explanation of the failure
 * @param requestId request correlation identifier
 * @param details structured non-sensitive error context, when provided
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminErrorEnvelopeV1(
        @JsonProperty("contract_version") String contractVersion,
        String code,
        String message,
        @JsonProperty("request_id") String requestId,
        Map<String, Object> details) {}

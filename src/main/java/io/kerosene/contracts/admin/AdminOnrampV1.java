package io.kerosene.contracts.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Administrative view of a fiat-to-Bitcoin on-ramp order.
 * Optional user and timestamp values may be absent; identifiers, currency, provider,
 * status, and a positive satoshi amount are required. JSON property names use snake_case.
 *
 * @param contractVersion schema version understood by producer and consumer
 * @param requestId identifier correlating this view to the administrative request
 * @param orderId provider or platform identifier for the on-ramp order
 * @param userId platform user associated with the order, when available
 * @param fiatCurrency ISO-style fiat currency code used for the purchase
 * @param fiatAmount fiat amount represented as text to preserve decimal precision
 * @param sats Bitcoin value credited or expected, in satoshis; must be positive
 * @param provider name of the external on-ramp provider
 * @param status provider/platform lifecycle state of the order
 * @param createdAt order creation timestamp, when supplied by the producer
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminOnrampV1(
        @JsonProperty("contract_version") String contractVersion,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("order_id") String orderId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("fiat_currency") String fiatCurrency,
        @JsonProperty("fiat_amount") String fiatAmount,
        long sats,
        String provider,
        String status,
        @JsonProperty("created_at") String createdAt) {

    /** Validates the required contract identifiers, currency, amount, provider, and status. */
    public AdminOnrampV1 {
        if (contractVersion == null || contractVersion.isBlank())
            throw new IllegalArgumentException("contractVersion required");
        if (requestId == null || requestId.isBlank())
            throw new IllegalArgumentException("requestId required");
        if (orderId == null || orderId.isBlank())
            throw new IllegalArgumentException("orderId required");
        if (fiatCurrency == null || fiatCurrency.isBlank())
            throw new IllegalArgumentException("fiatCurrency required");
        if (sats <= 0) throw new IllegalArgumentException("sats must be > 0");
        if (provider == null || provider.isBlank())
            throw new IllegalArgumentException("provider required");
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status required");
    }
}

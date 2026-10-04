package com.kerosene.common.financial.operations;

import java.math.BigDecimal;

/** Reads reference asset prices from the configured market-data source. */
public interface FinancialTickerPort {

    /** Returns the current BTC price denominated in the requested fiat currency.
     * @param currency fiat currency code, such as {@code USD} or {@code BRL}
     * @return positive or unavailable price according to adapter policy
     */
    BigDecimal getPrice(String currency);
}

package com.kerosene.common.financial.model;

/**
 * Non-negative Bitcoin amount represented in indivisible satoshis.
 * @param sats integer amount where one BTC equals 100,000,000 satoshis
 */
public record SatoshiAmount(long sats) {
    /** Rejects negative values; zero is valid for balance and fee snapshots.
     * @param sats amount to validate
     * @throws IllegalArgumentException if the amount is negative
     */
    public SatoshiAmount {
        if (sats < 0) throw new IllegalArgumentException("sats must be >= 0, got: " + sats);
    }

    @Override
    /** Returns the amount as base-10 integer text.
     * @return satoshi count without a unit suffix
     */
    public String toString() { return String.valueOf(sats); }
}

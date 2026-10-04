package com.kerosene.common.financial.operations;

import java.time.Instant;
import java.util.List;

/** Reports provider health with independent operational capability flags. */
public interface FinancialRailHealthPort {

    /** Returns health details for the custody provider.
     * @return typed health snapshot adapted from the implementation's provider report
     */
    ProviderHealth custodyProviderHealth();

    /** Returns health details for each configured active financial rail.
     * @return immutable list of typed rail-provider health snapshots
     */
    List<ProviderHealth> activeRailProviderHealth();

    /** Normalized operational state for a provider, separate from its individual capabilities. */
    enum HealthState {
        /** Provider is intentionally disabled by configuration or policy. */
        DISABLED,
        /** Provider has started but has not completed its readiness checks. */
        STARTING,
        /** Provider is available with reduced operational capability. */
        DEGRADED,
        /** Provider can observe state but is not authorized or able to spend. */
        READ_ONLY,
        /** Provider can perform the expected operations for the configured network. */
        AVAILABLE,
        /** Provider responds but a security condition makes it unsafe to use. */
        UNSAFE,
        /** Provider cannot be reached or cannot serve the platform. */
        UNAVAILABLE
    }

    /** Provider health snapshot with separate read, receive, spend, and reconciliation capabilities.
     * @param providerName configured provider identifier
     * @param implementation implementation name and version
     * @param state normalized provider health state
     * @param canRead whether the provider can query chain or account state
     * @param canReceive whether the provider can detect incoming transfers
     * @param canSpend whether the provider can create or sign outbound transfers
     * @param canReconcile whether the provider can support balance reconciliation
     * @param network provider's configured network
     * @param syncHeight latest synchronized block height, when applicable
     * @param lastSuccessfulProbe timestamp of the latest passing health probe
     * @param reasonCode stable explanation for a non-available state
     */
    record ProviderHealth(
        String providerName,         // e.g. "bitcoind-mainnet", "lnd-testnet"
        String implementation,       // e.g. "bitcoind:v26.0", "lnd:v0.18"
        HealthState state,
        boolean canRead,             // can query blockchain state
        boolean canReceive,          // can detect incoming transactions
        boolean canSpend,            // can create/sign outgoing transactions
        boolean canReconcile,        // can verify balances
        String network,              // MAINNET/TESTNET/etc
        long syncHeight,             // current block height
        Instant lastSuccessfulProbe, // last health check that passed
        String reasonCode            // why in current state (if not AVAILABLE)
    ) {
        /** Requires a provider name and state and prevents negative synchronization heights. */
        public ProviderHealth {
            if (providerName == null || providerName.isBlank()) throw new IllegalArgumentException("providerName required");
            if (state == null) throw new IllegalArgumentException("state required");
            if (syncHeight < 0) throw new IllegalArgumentException("syncHeight must be >= 0");
        }
    }
}

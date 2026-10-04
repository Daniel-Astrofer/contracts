package com.kerosene.common.financial.operations;

import java.time.Instant;
import java.util.List;

/** Exposes typed operational status, bounded logs, and process metrics to authorized operators. */
public interface FinancialOperationsAdminPortV2 {

    /** Returns the current Bitcoin node network and synchronization status.
     * @return blockchain status snapshot
     */
    BlockchainStatus blockchain();
    /** Returns the current Lightning node channel and synchronization status.
     * @return Lightning status snapshot
     */
    LightningStatus lightning();
    /** Reads a bounded page of redacted operational log entries.
     * @param request filters and page-size constraints
     * @return matching log entries and pagination metadata
     */
    PaginatedLogs logs(LogsRequest request);
    /** Returns selected process resource measurements.
     * @return system metrics snapshot
     */
    SystemMetrics metrics();

    /** Bitcoin network synchronization and node version snapshot.
     * @param network configured Bitcoin network name
     * @param blockHeight highest synchronized block height
     * @param peerCount connected peer count
     * @param synced whether the node satisfies its synchronization criterion
     * @param lastBlockTime time of the latest observed block, when available
     * @param version node software version
     */
    record BlockchainStatus(
        String network,
        int blockHeight,
        int peerCount,
        boolean synced,
        Instant lastBlockTime,
        String version
    ) {
        /** Requires a network and non-negative block and peer counts. */
        public BlockchainStatus {
            if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
            if (blockHeight < 0) throw new IllegalArgumentException("blockHeight must be >= 0");
            if (peerCount < 0) throw new IllegalArgumentException("peerCount must be >= 0");
        }
    }

    /** Lightning network connectivity, channel capacity, and chain-sync snapshot.
     * @param network configured Lightning network name
     * @param activeChannels number of channels currently usable
     * @param pendingChannels number of channels in opening or closing workflows
     * @param totalCapacitySats combined channel capacity in satoshis
     * @param syncedToChain whether the node is synchronized with its backing chain
     * @param version Lightning implementation version
     */
    record LightningStatus(
        String network,
        int activeChannels,
        int pendingChannels,
        long totalCapacitySats,
        boolean syncedToChain,
        String version
    ) {
        /** Requires network data and prevents negative channel counts or capacity. */
        public LightningStatus {
            if (network == null || network.isBlank()) throw new IllegalArgumentException("network required");
            if (activeChannels < 0) throw new IllegalArgumentException("activeChannels must be >= 0");
            if (pendingChannels < 0) throw new IllegalArgumentException("pendingChannels must be >= 0");
            if (totalCapacitySats < 0) throw new IllegalArgumentException("totalCapacitySats must be >= 0");
        }
    }

    /** One sanitized operational log row suitable for administrative inspection.
     * @param timestamp event time
     * @param level severity label such as INFO, WARN, or ERROR
     * @param source class, component, or subsystem that emitted the event
     * @param message redacted human-readable summary; raw secrets must not be included
     * @param correlationId request or event correlation identifier, when available
     */
    record LogEntry(
        Instant timestamp,
        String level,       // INFO, WARN, ERROR
        String source,      // class or component
        String message,     // redacted — never raw secrets
        String correlationId
    ) {
        /** Requires severity, safe message, and timestamp. */
        public LogEntry {
            if (level == null || level.isBlank()) throw new IllegalArgumentException("level required");
            if (message == null || message.isBlank()) throw new IllegalArgumentException("message required");
            if (timestamp == null) throw new IllegalArgumentException("timestamp required");
        }
    }

    /** A page of log entries with the query's total count and paging coordinates.
     * @param entries selected page; null is normalized to an empty list
     * @param total total matching entries before paging
     * @param page zero-based page index
     * @param pageSize maximum entries requested per page
     */
    record PaginatedLogs(
        List<LogEntry> entries,
        int total,
        int page,
        int pageSize
    ) {
        /** Normalizes null entries and validates the paging coordinates. */
        public PaginatedLogs {
            if (entries == null) entries = List.of();
            if (page < 0) throw new IllegalArgumentException("page must be >= 0");
            if (pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");
        }
    }

    /** Bounded query for the administrative log view.
     * @param limit maximum number of rows, from 1 through 1000
     * @param level optional exact or implementation-defined severity filter
     * @param correlationId optional request/event correlation filter
     */
    record LogsRequest(
        int limit,
        String level,           // optional filter
        String correlationId    // optional filter
    ) {
        /** Rejects limits outside the service's bounded query range. */
        public LogsRequest {
            if (limit < 1 || limit > 1000) throw new IllegalArgumentException("limit must be 1-1000");
        }
    }

    /** Process resource measurements captured for an administrative status response.
     * @param heapUsedBytes currently occupied JVM heap
     * @param heapMaxBytes configured or reported maximum JVM heap
     * @param cpuLoad normalized processor load measurement
     * @param activeThreads currently active JVM threads
     * @param openFileDescriptors open process file descriptors
     * @param uptimeSeconds process uptime in seconds
     */
    record SystemMetrics(
        long heapUsedBytes,
        long heapMaxBytes,
        double cpuLoad,
        int activeThreads,
        int openFileDescriptors,
        long uptimeSeconds
    ) {
        /** Rejects impossible negative memory and thread measurements. */
        public SystemMetrics {
            if (heapUsedBytes < 0) throw new IllegalArgumentException("heapUsedBytes must be >= 0");
            if (cpuLoad < 0) throw new IllegalArgumentException("cpuLoad must be >= 0");
            if (activeThreads < 0) throw new IllegalArgumentException("activeThreads must be >= 0");
        }
    }
}

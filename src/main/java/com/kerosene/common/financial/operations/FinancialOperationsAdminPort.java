package com.kerosene.common.financial.operations;

import java.util.List;
import java.util.Map;

/** Operations administration port providing runtime blockchain and Lightning node health, logs, and metrics. */
public interface FinancialOperationsAdminPort {

    /** Returns current blockchain node network and synchronization status.
     * @return operational status map
     */
    Map<String, Object> blockchain();

    /** Returns current Lightning node channel and connectivity status.
     * @return operational status map
     */
    Map<String, Object> lightning();

    /** Reads bounded operational log entries.
     * @param limit maximum number of entries to return
     * @return list of sanitized log maps
     */
    List<Map<String, Object>> logs(int limit);

    /** Returns system and transaction aggregate metrics.
     * @return operational metrics map
     */
    Map<String, Object> metrics();
}

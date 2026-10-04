package com.kerosene.common.financial.operations;

import java.util.List;

/** Evaluates production financial-rail configuration against fail-safe operating requirements. */
public interface FinancialRailProductionSafetyPort {

    /** Collects configuration and readiness violations that make production rails unsafe.
     * @return list of human-readable violation descriptions; empty means no detected violation
     */
    List<String> collectProductionViolations();
}

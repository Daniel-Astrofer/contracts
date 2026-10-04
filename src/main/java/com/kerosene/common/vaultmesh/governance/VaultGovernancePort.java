package com.kerosene.common.vaultmesh.governance;

/** Performs day-epoch governance and key-resharing operations with the custody mesh. */
public interface VaultGovernancePort {
    /** Reads the observed and required mesh day epoch.
     * @return status distinguishing current, stale, and failed reads
     */
    VaultMeshDayStatus getDayStatus();
    /** Submits a member vote for an epoch transition.
     * @param proposalHash digest of the day-advance proposal
     * @param signature member signature over the proposal
     * @return vote acceptance or failure status
     */
    VaultMeshDayAdvanceResult voteDay(String proposalHash, String signature);
    /** Applies the day transition once the mesh governance threshold is satisfied.
     * @return advancement outcome and resulting epoch, when advanced
     */
    VaultMeshDayAdvanceResult advanceDay();
    /** Requests key resharing under the current governance policy.
     * @param reason operator or policy rationale for resharing
     * @return resharing success or failure details
     */
    VaultMeshReshareResult triggerReshare(String reason);
}

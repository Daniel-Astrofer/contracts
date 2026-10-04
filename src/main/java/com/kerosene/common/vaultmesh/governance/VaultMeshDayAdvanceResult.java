package com.kerosene.common.vaultmesh.governance;

/**
 * Outcome of {@code POST /v1/day/vote} or {@code POST /v1/day/advance}.
 * Successful results carry the current epoch and whether it advanced; failures carry an error.
 * @param dayEpoch resulting epoch identifier, null when the operation failed
 * @param advanced whether the epoch changed as part of this operation
 * @param ok whether the vote/advance operation completed successfully
 * @param error failure reason, null on success
 */
public record VaultMeshDayAdvanceResult(
        String dayEpoch,
        boolean advanced,
        boolean ok,
        String error
) {
    /** Prevents a success response from also carrying a failure.
     * @param dayEpoch resulting epoch
     * @param advanced whether it advanced
     * @param ok operation success flag
     * @param error failure detail
     * @throws IllegalArgumentException if success and error are both set
     */
    public VaultMeshDayAdvanceResult {
        if (ok && error != null) throw new IllegalArgumentException("ok and error cannot both be set");
    }

    /** Creates a successful epoch-advance response.
     * @param dayEpoch non-blank resulting epoch identifier
     * @param advanced whether this operation changed the epoch
     * @return successful result
     * @throws IllegalArgumentException if dayEpoch is blank
     */
    public static VaultMeshDayAdvanceResult ok(String dayEpoch, boolean advanced) {
        if (dayEpoch == null || dayEpoch.isBlank()) throw new IllegalArgumentException("dayEpoch required");
        return new VaultMeshDayAdvanceResult(dayEpoch, advanced, true, null);
    }

    /** Creates a failed advance response.
     * @param error non-blank failure explanation
     * @return failed result with no epoch value
     * @throws IllegalArgumentException if error is blank
     */
    public static VaultMeshDayAdvanceResult failed(String error) {
        if (error == null || error.isBlank()) throw new IllegalArgumentException("error required");
        return new VaultMeshDayAdvanceResult(null, false, false, error);
    }
}

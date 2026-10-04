package com.kerosene.common.vaultmesh.governance;

/**
 * Snapshot of vault-mesh day_epoch relative to the caller's UTC calendar day.
 * A failed lookup uses only {@code error}; a stale snapshot reports both the observed and needed epochs.
 * @param dayEpoch epoch currently stored by the mesh, null if unavailable
 * @param neededDayEpoch epoch the caller expects for the current UTC day, null when not evaluated
 * @param upToDate true when the current epoch matches the expected epoch
 * @param stale true when the current epoch is behind the expected epoch
 * @param error lookup or protocol failure, null when comparison succeeded
 */
public record VaultMeshDayStatus(
        String dayEpoch,
        String neededDayEpoch,
        boolean upToDate,
        boolean stale,
        String error
) {
    /** Rejects contradictory current and stale flags.
     * @param dayEpoch observed epoch
     * @param neededDayEpoch expected epoch
     * @param upToDate whether epochs match
     * @param stale whether the observed epoch is outdated
     * @param error failure detail
     * @throws IllegalArgumentException when both upToDate and stale are true
     */
    public VaultMeshDayStatus {
        // error is the only non-null field when failed
        if (upToDate && stale) throw new IllegalArgumentException("upToDate and stale cannot both be true");
    }

    /** Creates a matching-epoch status.
     * @param dayEpoch non-blank epoch that is current for the caller's UTC day
     * @return up-to-date status with observed and needed epochs equal
     * @throws IllegalArgumentException if dayEpoch is blank
     */
    public static VaultMeshDayStatus upToDate(String dayEpoch) {
        if (dayEpoch == null || dayEpoch.isBlank()) throw new IllegalArgumentException("dayEpoch required");
        return new VaultMeshDayStatus(dayEpoch, dayEpoch, true, false, null);
    }

    /** Creates a stale status describing the observed and required epochs.
     * @param have non-blank epoch currently held by the mesh
     * @param need non-blank epoch required for the current UTC day
     * @return stale status
     * @throws IllegalArgumentException if either epoch is blank
     */
    public static VaultMeshDayStatus stale(String have, String need) {
        if (have == null || have.isBlank()) throw new IllegalArgumentException("have dayEpoch required");
        if (need == null || need.isBlank()) throw new IllegalArgumentException("need dayEpoch required");
        return new VaultMeshDayStatus(have, need, false, true, null);
    }

    /** Creates a status representing an inability to retrieve or compare epochs.
     * @param error non-blank failure explanation
     * @return failure status with epoch values absent
     * @throws IllegalArgumentException if error is blank
     */
    public static VaultMeshDayStatus failed(String error) {
        if (error == null || error.isBlank()) throw new IllegalArgumentException("error required");
        return new VaultMeshDayStatus(null, null, false, false, error);
    }
}

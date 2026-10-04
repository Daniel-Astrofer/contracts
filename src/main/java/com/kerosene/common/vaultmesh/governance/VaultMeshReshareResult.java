package com.kerosene.common.vaultmesh.governance;

/**
 * Outcome of {@code POST /v1/reshare/trigger}.
 * A successful result reports the new policy and reason; a failed result carries an error.
 *
 * @param reshared whether a new share distribution was actually produced
 * @param policy policy identifier used for the resulting share set, when successful
 * @param reason explanation for the requested or completed resharing operation
 * @param ok whether the trigger operation completed without an error
 * @param error failure description; must be null when {@code ok} is true
 */
public record VaultMeshReshareResult(
        boolean reshared,
        String policy,
        String reason,
        boolean ok,
        String error
) {
    /** Ensures a successful outcome does not also contain a failure. */
    public VaultMeshReshareResult {
        if (ok && error != null) throw new IllegalArgumentException("ok and error cannot both be set");
    }

    /** Creates a successful outcome with the policy and reason that produced the new shares.
     * @param policy resulting policy identifier
     * @param reason reason for resharing
     * @return successful resharing result
     * @throws IllegalArgumentException if policy or reason is blank
     */
    public static VaultMeshReshareResult ok(String policy, String reason) {
        if (policy == null || policy.isBlank()) throw new IllegalArgumentException("policy required");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason required");
        return new VaultMeshReshareResult(true, policy, reason, true, null);
    }

    /** Creates a failed outcome without a policy or success reason.
     * @param error description of why resharing failed
     * @return failed resharing result
     * @throws IllegalArgumentException if the error is blank
     */
    public static VaultMeshReshareResult failed(String error) {
        if (error == null || error.isBlank()) throw new IllegalArgumentException("error required");
        return new VaultMeshReshareResult(false, null, null, false, error);
    }
}

package com.kerosene.common.vaultmesh.intent;

/** Manages temporary and final reservations of custody funds for dependent workflows. */
public interface VaultReservationPort {
    /** Places a soft reservation before beginning an external operation.
     * @param intent signed intent describing the funds and policy
     * @return reservation result and token needed for later release or commit
     */
    VaultMeshReceipt reserveIntent(VaultMeshIntentV2 intent);
    /** Releases an uncommitted reservation after the dependent operation fails or is abandoned.
     * @param intentId reserved intent identifier
     * @param reservationToken opaque token proving reservation ownership
     * @return release outcome receipt
     */
    VaultMeshReceipt releaseIntent(String intentId, String reservationToken);
    /** Commits a reservation after the dependent external operation succeeds.
     * @param intentId reserved intent identifier
     * @param reservationToken opaque token returned by the reserve operation
     * @return commit outcome receipt
     */
    VaultMeshReceipt commitIntent(String intentId, String reservationToken);
}

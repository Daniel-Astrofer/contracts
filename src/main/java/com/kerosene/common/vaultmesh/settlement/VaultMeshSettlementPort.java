package com.kerosene.common.vaultmesh.settlement;

import com.kerosene.common.vaultmesh.governance.VaultGovernancePort;
import com.kerosene.common.vaultmesh.governance.VaultMeshDayAdvanceResult;
import com.kerosene.common.vaultmesh.governance.VaultMeshDayStatus;
import com.kerosene.common.vaultmesh.governance.VaultMeshReshareResult;
import com.kerosene.common.vaultmesh.intent.VaultIntentPort;
import com.kerosene.common.vaultmesh.intent.VaultMeshIntent;
import com.kerosene.common.vaultmesh.intent.VaultMeshIntentV2;
import com.kerosene.common.vaultmesh.intent.VaultMeshReceipt;
import com.kerosene.common.vaultmesh.intent.VaultReservationPort;

/**
 * Consolidated settlement port for {@code kfe-service} to submit settlement intents,
 * manage reservations, request PSBT signing, observe governance, and query deposit descriptors.
 * Implementations live in adapters only (Clean Architecture / DIP).
 */
public interface VaultMeshSettlementPort {

    /** Submits a one-shot settlement intent to the legacy aggregate adapter.
     * @param intent intent describing destination, amount, and custody policy
     * @return vault outcome receipt
     */
        VaultMeshReceipt submitIntent(VaultMeshIntent intent);

    /** Reserves funds for a settlement intent without final commitment.
     * @param intent intent whose funds should be reserved
     * @return reservation outcome receipt; the default reports unsupported operation
     */
        default VaultMeshReceipt reserveIntent(VaultMeshIntent intent) {
        return new VaultMeshReceipt(intent == null ? "unknown" : intent.intentId(),
                VaultMeshReceipt.Status.REJECTED, "MESH_INTENT_RESERVE_UNSUPPORTED",
                null, java.time.Instant.now());
    }

    /** Releases a prior soft reservation and returns the funds to the bucket.
     * @param intentId reserved intent identifier
     * @param bucket custody bucket holding the reservation
     * @param amountSats reserved amount to release, in satoshis
     * @return release outcome receipt; the default reports unsupported operation
     */
        default VaultMeshReceipt releaseIntent(String intentId, String bucket, long amountSats) {
        return new VaultMeshReceipt(intentId, VaultMeshReceipt.Status.REJECTED,
                "MESH_INTENT_RELEASE_UNSUPPORTED", null, java.time.Instant.now());
    }

    /** Permanently consumes a prior reservation after the dependent operation succeeds.
     * @param intentId reserved intent identifier to commit
     * @return commit outcome receipt; the default reports unsupported operation
     */
        default VaultMeshReceipt commitIntent(String intentId) {
        return new VaultMeshReceipt(intentId, VaultMeshReceipt.Status.REJECTED,
                "MESH_INTENT_COMMIT_UNSUPPORTED", null, java.time.Instant.now());
    }

    /** Requests signing of a PSBT through the legacy aggregate adapter.
     * @param request transaction and intent-bound PSBT request
     * @return signing receipt; the default reports unsupported operation
     */
        default VaultMeshPsbtReceipt signPsbt(VaultMeshPsbtRequest request) {
        return new VaultMeshPsbtReceipt(request == null ? "unknown" : request.intentId(),
                VaultMeshReceipt.Status.REJECTED, "MESH_PSBT_UNSUPPORTED",
                null, null, java.time.Instant.now());
    }

    /** Reads the mesh's current day-epoch status.
     * @return current status or an unsupported-operation failure result
     */
        default VaultMeshDayStatus getDayStatus() {
        return VaultMeshDayStatus.failed("MESH_DAY_UNSUPPORTED");
    }

    /** Casts a member vote to advance the mesh day epoch.
     * @param voter configured member identifier casting the vote
     * @param dayEpoch epoch proposed for advancement
     * @return vote outcome or an unsupported-operation failure result
     */
        default VaultMeshDayAdvanceResult voteDay(String voter, String dayEpoch) {
        return VaultMeshDayAdvanceResult.failed("MESH_DAY_UNSUPPORTED");
    }

    /** Attempts to advance the epoch after governance requirements have been met.
     * @return advancement outcome or an unsupported-operation failure result
     */
        default VaultMeshDayAdvanceResult advanceDay() {
        return VaultMeshDayAdvanceResult.failed("MESH_DAY_UNSUPPORTED");
    }

    /** Requests a resharing ceremony for the custody key set.
     * @param reason operator or policy reason for resharing
     * @return resharing outcome or an unsupported-operation failure result
     */
        default VaultMeshReshareResult triggerReshare(String reason) {
        return VaultMeshReshareResult.failed("MESH_RESHARE_UNSUPPORTED");
    }

    /** Retrieves the shared deposit descriptor and address for user funds.
     * @return user deposit information, or null when the legacy adapter does not support it
     */
        default VaultMeshDepositInfo getUsersDepositAddress() {
        return null;
    }

    /** Retrieves the shared deposit descriptor and address for channel liquidity.
     * @return channel deposit information, or null when the legacy adapter does not support it
     */
        default VaultMeshDepositInfo getChannelsDepositAddress() {
        return null;
    }
}

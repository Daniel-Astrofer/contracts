package com.kerosene.common.vaultmesh.intent;

/** Submits signed, versioned settlement intents to the custody mesh. */
public interface VaultIntentPort {
    /** Requests policy validation and settlement of an intent.
     * @param intent fully bound and signed version-2 intent
     * @return receipt describing accepted, rejected, or fail-stop outcome
     */
    VaultMeshReceipt submitIntent(VaultMeshIntentV2 intent);
}

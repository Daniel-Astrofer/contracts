package com.kerosene.common.vaultmesh.settlement;

/** Requests custody-mesh validation and signing of transaction PSBTs. */
public interface VaultPsbtSigningPort {
    /** Validates intent binding, policy, fee limits, and output constraints before signing.
     * @param request versioned PSBT request with intent and user-authorization context
     * @return accepted signed-PSBT proof, rejection details, or fail-stop status
     */
    VaultMeshPsbtResult signPsbt(VaultMeshPsbtRequestV2 request);
}

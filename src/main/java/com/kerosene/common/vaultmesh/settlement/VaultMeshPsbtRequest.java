package com.kerosene.common.vaultmesh.settlement;

/**
 * Request for vault-mesh Taproot / PSBT signing under Intent gate.
 *
 * <p>{@code commitIntent=false} is for CHANNELS→LND inject: Intent is already soft-reserved;
 * mesh signs without durable-consuming so open failure can still release.
 * @param intentId unique spend intent being authorized
 * @param sessionId signing session associated with the spend
 * @param bucket custody bucket from which the output is funded
 * @param destination recipient address or channel destination
 * @param amountSats amount authorized in satoshis
 * @param psbtBase64 unsigned PSBT supplied for vault signing
 * @param commitIntent whether signing should durably consume the intent; null defaults to true
 */
public record VaultMeshPsbtRequest(
        String intentId,
        String sessionId,
        String bucket,
        String destination,
        long amountSats,
        String psbtBase64,
        Boolean commitIntent
) {
    /** Creates a request using the default durable intent-commit behavior.
     * @param intentId spend intent identifier
     * @param sessionId signing session identifier
     * @param bucket custody bucket
     * @param destination recipient destination
     * @param amountSats amount in satoshis
     * @param psbtBase64 unsigned PSBT text
     */
    public VaultMeshPsbtRequest(
            String intentId,
            String sessionId,
            String bucket,
            String destination,
            long amountSats,
            String psbtBase64) {
        this(intentId, sessionId, bucket, destination, amountSats, psbtBase64, Boolean.TRUE);
    }

    /** Resolves the nullable wire flag to its effective behavior.
     * @return {@code false} only when the request explicitly disables commit; otherwise {@code true}
     */
    public boolean shouldCommitIntent() {
        return commitIntent == null || commitIntent;
    }
}

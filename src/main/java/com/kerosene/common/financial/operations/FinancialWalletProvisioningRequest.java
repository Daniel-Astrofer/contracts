package com.kerosene.common.financial.operations;

/** Request to provision a wallet for a platform user.
 * @param userId owner of the wallet to provision
 * @param initialAddress optional initial receive address; null when the wallet has none yet
 */
public record FinancialWalletProvisioningRequest(Long userId, String initialAddress) {

    /** Requires an owner and rejects an explicitly blank initial address.
     * @param userId wallet owner identifier
     * @param initialAddress optional starting address
     * @throws IllegalArgumentException if userId is absent or address is blank
     */
    public FinancialWalletProvisioningRequest {
        if (userId == null) throw new IllegalArgumentException("userId required");
        if (initialAddress != null && initialAddress.isBlank()) {
            throw new IllegalArgumentException("initialAddress must be null or non-blank");
        }
    }
}

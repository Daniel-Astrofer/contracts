package com.kerosene.common.financial.operations;

import java.util.UUID;

/** Provisions wallet signing keys through an MPC custody implementation. */
public interface FinancialMpcKeyPort {

    /** Creates an MPC key and returns its public derivation and attestation metadata.
     * @param request wallet, network, scheme, and key-purpose request
     * @return receipt identifying the generated key and its attestation
     * @throws UnsupportedOperationException when the adapter lacks typed MPC provisioning
     */
    default MpcWalletKeyReceipt provisionWalletKey(MpcWalletKeyRequest request) {
        throw new UnsupportedOperationException("Typed MPC wallet provisioning is not implemented by this adapter");
    }



    /**
     * Generates or derives an MPC wallet key for the specified wallet and user.
     * @param walletId wallet receiving the generated key
     * @param userId wallet owner
     * @return generated public key or key material
     */
    String keygenWallet(UUID walletId, Long userId);
}

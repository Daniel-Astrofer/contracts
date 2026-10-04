package com.kerosene.common.vaultmesh.settlement;

import java.util.Optional;

/** Reads network-specific shared deposit descriptors from the custody mesh. */
public interface VaultDepositDescriptorPort {
    /** Retrieves the Taproot destination used for user wallet deposits.
     * @return deposit info when the mesh has a descriptor, otherwise empty
     */
    Optional<VaultMeshDepositInfo> getUsersDepositAddress();
    /** Retrieves the Taproot destination used for channel liquidity deposits.
     * @return deposit info when the mesh has a descriptor, otherwise empty
     */
    Optional<VaultMeshDepositInfo> getChannelsDepositAddress();
}

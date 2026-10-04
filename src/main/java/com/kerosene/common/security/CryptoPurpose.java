package com.kerosene.common.security;

/**
 * Declares the cryptographic purpose for domain separation.
 * Combined with table+column+entityId+tenantId for AAD.
 */
public enum CryptoPurpose {
    /** Encrypts database column values with entity-specific authenticated context. */
    COLUMN_ENCRYPTION,
    /** Encrypts wallet private or recovery secrets. */
    WALLET_SECRET,
    /** Encrypts external provider credentials such as API keys. */
    API_KEY,
    /** Encrypts authentication tokens or token-like bearer material. */
    TOKEN
}

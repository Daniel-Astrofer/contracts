package com.kerosene.common.security;

/**
 * Port for column-level encryption using KMS-bound keys.
 *
 * <p>The master key never leaves the KMS/HSM boundary.
 * All operations use {@link EncryptedValue} with AAD context binding.
 *
 * <h2>API Flavors</h2>
 * <p>Supports purpose-bound encryption using
 * {@link #encrypt(CryptoPurpose, byte[], byte[])} and
 * {@link #decrypt(CryptoPurpose, EncryptedValue, byte[])},
 * as well as direct column crypto for persistence converters.</p>
 */
public interface StringColumnCryptoPort {

    // ── New API (purpose-bound, KMS-safe) ────────────────────────────────

    /**
     * Encrypts plaintext with AAD context binding.
     *
     * @param purpose       domain separation tag
     * @param plaintext     the data to encrypt
     * @param associatedData additional authenticated data (table+column+entityId+tenantId)
     * @return sealed encrypted container
     */
    default EncryptedValue encrypt(CryptoPurpose purpose, byte[] plaintext, byte[] associatedData) {
        throw new UnsupportedOperationException("Purpose-bound encryption is not implemented by this adapter");
    }

    /**
     * Decrypts an {@link EncryptedValue} with AAD context binding.
     *
     * @param purpose       must match the purpose used at encryption time
     * @param encrypted     the sealed container
     * @param associatedData must match the AAD used at encryption time
     * @return original plaintext
     */
    default byte[] decrypt(CryptoPurpose purpose, EncryptedValue encrypted, byte[] associatedData) {
        throw new UnsupportedOperationException("Purpose-bound decryption is not implemented by this adapter");
    }

    /**
     * Re-wraps (rotates) the encrypted value without exposing plaintext.
     * Used for key rotation when the underlying KMS key changes.
     *
     * @param value the currently sealed value
     * @return a new EncryptedValue sealed under the current KMS key version
     */
    default EncryptedValue rewrap(EncryptedValue value) {
        throw new UnsupportedOperationException("Ciphertext rewrap is not implemented by this adapter");
    }

    /**
     * Checks whether the encrypted value should be rotated.
     *
     * @param value the currently sealed value
     * @return true if the KMS key version or algorithm is stale
     */
    default boolean needsRotation(EncryptedValue value) {
        throw new UnsupportedOperationException("Key rotation check is not implemented by this adapter");
    }

    // ── Direct / persistence column crypto API ──────────────────────────

    /**
     * Direct encryption of plaintext bytes without additional authenticated data.
     * @param plainBytes plaintext bytes to encrypt
     * @return ciphertext string
     */
    default String encrypt(byte[] plainBytes) {
        throw new UnsupportedOperationException("Direct encrypt(byte[]) is not supported by this implementation.");
    }

    /**
     * Direct decryption of ciphertext string.
     * @param encryptedValue ciphertext representation
     * @return decrypted plaintext bytes
     */
    default byte[] decrypt(String encryptedValue) {
        throw new UnsupportedOperationException("Direct decrypt(String) is not supported by this implementation.");
    }

    /**
     * Returns raw master key bytes for direct symmetric cryptographic operations.
     * @return master key bytes
     */
    default byte[] getMasterKeyBytes() {
        throw new UnsupportedOperationException("getMasterKeyBytes() is not supported by this implementation.");
    }
}

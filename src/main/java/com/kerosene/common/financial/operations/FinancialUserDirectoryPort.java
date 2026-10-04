package com.kerosene.common.financial.operations;

import java.util.Optional;

/**
 * Minimal user directory contract exposed to financial services.
 *
 * <p>KFE must not depend on auth persistence entities or repositories. This
 * port carries only the identity data required for financial ownership lookup.</p>
 */
public interface FinancialUserDirectoryPort {

    /** Resolves a financial owner by exact username without exposing auth persistence models.
     * @param username account username to look up
     * @return matching identity, or empty when no active or known user exists
     */
    Optional<FinancialUserHandle> findByUsername(String username);

    /** Resolves a financial owner by persistent user identifier.
     * @param userId platform user ID to look up
     * @return matching identity, or empty when no user exists
     */
    Optional<FinancialUserHandle> findById(Long userId);

    /** Minimal identity projection required for financial ownership checks.
     * @param id stable platform user identifier
     * @param username username used by financial workflows and audits
     * @param active whether the account is currently allowed to act
     */
    record FinancialUserHandle(Long id, String username, boolean active) {
    }
}

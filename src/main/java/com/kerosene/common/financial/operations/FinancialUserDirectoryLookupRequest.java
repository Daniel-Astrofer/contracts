package com.kerosene.common.financial.operations;

/**
 * Internal KFE-to-Core user lookup request.
 *
 * <p>Exactly one lookup key must be provided.</p>
 * @param username exact username lookup key, null when searching by user ID
 * @param userId exact persistent user ID lookup key, null when searching by username
 */
public record FinancialUserDirectoryLookupRequest(String username, Long userId) {

    /** Enforces that a lookup key is present.
     * @param username username lookup key
     * @param userId user ID lookup key
     * @throws IllegalArgumentException if both keys are null
     */
    public FinancialUserDirectoryLookupRequest {
        if (username == null && userId == null) {
            throw new IllegalArgumentException("exactly one of username or userId required");
        }
        // username is nullable when searching by userId
    }

    /** Creates a lookup request keyed by username.
     * @param username non-blank username to resolve
     * @return request with only the username key set
     * @throws IllegalArgumentException if username is null or blank
     */
    public static FinancialUserDirectoryLookupRequest byUsername(String username) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        return new FinancialUserDirectoryLookupRequest(username, null);
    }

    /** Creates a lookup request keyed by persistent user ID.
     * @param userId user identifier to resolve
     * @return request with only the user ID key set
     * @throws IllegalArgumentException if userId is null
     */
    public static FinancialUserDirectoryLookupRequest byUserId(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId required");
        return new FinancialUserDirectoryLookupRequest(null, userId);
    }
}

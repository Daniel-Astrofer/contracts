package com.kerosene.common.financial.stomp;

/**
 * Allow-listed user-scoped STOMP queues used by financial event notifications.
 */
public enum StompDestination {
    /** User queue carrying wallet balance changes. */
    QUEUE_BALANCE("/queue/balance"),
    /** User queue carrying transaction lifecycle events. */
    QUEUE_TRANSACTION("/queue/transaction"),
    /** User queue carrying general platform notifications. */
    QUEUE_NOTIFICATION("/queue/notification");

    private final String path;
    /** Associates this enum value with its exact broker-relative queue path.
     * @param path allow-listed STOMP destination path
     */
    StompDestination(String path) { this.path = path; }
    /** Returns the broker-relative queue path represented by this enum.
     * @return allow-listed destination path
     */
    public String path() { return path; }

    /** Resolves an exact allow-listed queue path to its typed destination.
     * @param path destination received from a caller
     * @return matching typed queue
     * @throws IllegalArgumentException if the path is not in the allowlist
     */
    public static StompDestination fromPath(String path) {
        for (StompDestination d : values()) {
            if (d.path.equals(path)) return d;
        }
        throw new IllegalArgumentException("Unknown STOMP destination: " + path);
    }
}

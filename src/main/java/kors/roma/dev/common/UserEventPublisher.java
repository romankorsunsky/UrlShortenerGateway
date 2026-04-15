package kors.roma.dev.common;

public interface UserEventPublisher{
    /**
     * Publish fanout message
     * @param user - data, already encoded, in byte array form
     * @param maxRetries - the messages are acknowledged, we also add NACKs on retries,
     */
    public void publish(byte[] data,int maxRetries);

    /**
     * Allegedly, returns if the instance is in a valid state
     * so that the PooledObject knows to be removed if not.
     * @return
     */
    public boolean isValid();
}

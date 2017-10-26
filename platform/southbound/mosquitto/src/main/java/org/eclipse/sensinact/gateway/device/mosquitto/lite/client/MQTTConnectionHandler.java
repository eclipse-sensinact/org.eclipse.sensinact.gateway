package org.eclipse.sensinact.gateway.device.mosquitto.lite.client;

public interface MQTTConnectionHandler {
    /**
     * Indicated that the connection failed on the first attempt.
     * @param connectionId
     */
    void connectionFailed(String connectionId);

    /**
     * Called when the connection is established, either first or consecutive time (if recovering from a disconnection)
     * @param connectionId
     */
    void connectionEstablished(String connectionId);

    /**
     * Method fired when the connection was established at some point but lost
     * @param connectionId
     */
    void connectionLost(String connectionId);

}

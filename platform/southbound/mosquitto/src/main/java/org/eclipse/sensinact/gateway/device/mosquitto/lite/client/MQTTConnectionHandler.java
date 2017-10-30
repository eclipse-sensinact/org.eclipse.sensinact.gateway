package org.eclipse.sensinact.gateway.device.mosquitto.lite.client;

public interface MQTTConnectionHandler {
    /**
     * Indicated that the connection failed on the first attempt.
     * @param connection
     */
    void connectionFailed(MQTTClient connection);

    /**
     * Called when the connection is established, either first or consecutive time (if recovering from a disconnection)
     * @param connection
     */
    void connectionEstablished(MQTTClient connection);

    /**
     * Method fired when the connection was established at some point but lost
     * @param connection
     */
    void connectionLost(MQTTClient connection);

}

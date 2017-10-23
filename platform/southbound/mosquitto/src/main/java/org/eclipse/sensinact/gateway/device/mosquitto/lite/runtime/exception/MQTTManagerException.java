package org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime.exception;

public class MQTTManagerException extends Exception {
    public MQTTManagerException() {
        super();
    }

    public MQTTManagerException(String message) {
        super(message);
    }

    public MQTTManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQTTManagerException(Throwable cause) {
        super(cause);
    }

    protected MQTTManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

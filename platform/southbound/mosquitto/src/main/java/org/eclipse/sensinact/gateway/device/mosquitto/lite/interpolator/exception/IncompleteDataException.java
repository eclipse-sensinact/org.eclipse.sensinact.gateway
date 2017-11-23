package org.eclipse.sensinact.gateway.device.mosquitto.lite.interpolator.exception;

/**
 * Created by nj246216 on 23/11/17.
 */
public class IncompleteDataException extends Exception {
    public IncompleteDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompleteDataException(Throwable cause) {
        super(cause);
    }

    public IncompleteDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IncompleteDataException(String message) {
        super(message);
    }

    public IncompleteDataException() {
        super();
    }
}

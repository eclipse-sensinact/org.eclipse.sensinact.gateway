package org.eclipse.sensinact.gateway.app.manager.application.persistence.exception;

public class ApplicationParseException extends Exception {
    public ApplicationParseException() {
        super();
    }

    public ApplicationParseException(String message) {
        super(message);
    }

    public ApplicationParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationParseException(Throwable cause) {
        super(cause);
    }

    protected ApplicationParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

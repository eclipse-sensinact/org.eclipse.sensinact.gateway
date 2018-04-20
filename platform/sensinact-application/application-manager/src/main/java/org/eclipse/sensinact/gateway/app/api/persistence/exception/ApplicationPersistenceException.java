package org.eclipse.sensinact.gateway.app.api.persistence.exception;

public class ApplicationPersistenceException extends Exception{

    public ApplicationPersistenceException() {
        super();
    }

    public ApplicationPersistenceException(String message) {
        super(message);
    }

    public ApplicationPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationPersistenceException(Throwable cause) {
        super(cause);
    }

    protected ApplicationPersistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

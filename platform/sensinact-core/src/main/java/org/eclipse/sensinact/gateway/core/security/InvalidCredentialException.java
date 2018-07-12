package org.eclipse.sensinact.gateway.core.security;

public class InvalidCredentialException extends SecuredAccessException {
    public InvalidCredentialException() {
        super();
    }

    public InvalidCredentialException(String message) {
        super(message);
    }
}

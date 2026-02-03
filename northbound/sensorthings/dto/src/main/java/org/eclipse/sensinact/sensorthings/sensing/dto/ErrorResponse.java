package org.eclipse.sensinact.sensorthings.sensing.dto;

public record ErrorResponse(Error error) {
    public record Error(String code, String message) {
    }

    public ErrorResponse(String code, String message) {
        this(new Error(code, message));
    }

}

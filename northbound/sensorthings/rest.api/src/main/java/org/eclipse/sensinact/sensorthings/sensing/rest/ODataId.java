package org.eclipse.sensinact.sensorthings.sensing.rest;

public record ODataId(String value) {

    public ODataId {
        // Strip quotes if present
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

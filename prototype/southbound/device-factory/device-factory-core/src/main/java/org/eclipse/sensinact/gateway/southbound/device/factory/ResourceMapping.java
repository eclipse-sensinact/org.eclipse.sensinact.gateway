/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.device.factory;

import java.util.Map;

/**
 * Parses a resource path and associates it to a record path
 */
public class ResourceMapping {

    /**
     * Resource service
     */
    private final String service;

    /**
     * Resource name
     */
    private final String resource;

    /**
     * Resource metadata name
     */
    private final String metadata;

    /**
     * Associated path in the record
     */
    private final RecordPath recordPath;

    /**
     * Parses the resource path
     *
     * @param rcPath     Mapping resource path
     * @param recordPath Mapping record path
     * @throws InvalidResourcePathException Error parsing the resource path
     */
    public ResourceMapping(final String rcPath, final RecordPath recordPath) throws InvalidResourcePathException {

        final String[] parts = rcPath.split("/");
        if (parts.length < 2) {
            throw new InvalidResourcePathException(String.format("Not enought parts in path '%s'", rcPath));
        } else if (parts.length > 3) {
            throw new InvalidResourcePathException(String.format("Too many parts in path '%s'", rcPath));
        }

        this.recordPath = recordPath;
        service = parts[0].strip();
        if (service.isEmpty()) {
            throw new InvalidResourcePathException(String.format("Empty service name in path '%s'", rcPath));
        }
        resource = parts[1].strip();
        if (resource.isEmpty()) {
            throw new InvalidResourcePathException(String.format("Empty resource name in path '%s'", rcPath));
        }

        if (parts.length == 3) {
            metadata = parts[2].strip();
            if (metadata.isEmpty()) {
                throw new InvalidResourcePathException(String.format("Empty metadata name in path '%s'", rcPath));
            }
        } else {
            metadata = null;
        }
    }

    /**
     * Returns the name of the service
     */
    public String getService() {
        return service;
    }

    /**
     * Returns the name of the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Returns the name of the metadata, if any
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Returns the associated path in the record
     */
    public RecordPath getRecordPath() {
        return recordPath;
    }

    /**
     * Checks if this mapping targets a metadata
     */
    public boolean isMetadata() {
        return metadata != null;
    }

    /**
     * Returns the resource path
     */
    public String getResourcePath() {
        String rcPath = service + "/" + resource;
        if (metadata != null) {
            rcPath += "/" + metadata;
        }

        return rcPath;
    }

    @Override
    public String toString() {
        return String.format("%s[%s -> %s]", getClass().getSimpleName(), getResourcePath(), getRecordPath());
    }

    /**
     * Returns a new instance with resolved variables
     *
     * @param variables Variables values
     * @throws InvalidResourcePathException Invalid resolved path
     * @throws VariableNotFoundException    Couldn't resolve a variable
     */
    public ResourceMapping fillInVariables(Map<String, String> variables)
            throws InvalidResourcePathException, VariableNotFoundException {

        RecordPath newRecordPath = recordPath.fillInVariables(variables);

        String newRcPath = getResourcePath();
        if (VariableSolver.containsVariables(newRcPath)) {
            newRcPath = VariableSolver.fillInVariables(newRcPath, variables);
        }

        return new ResourceMapping(newRcPath, newRecordPath);
    }
}

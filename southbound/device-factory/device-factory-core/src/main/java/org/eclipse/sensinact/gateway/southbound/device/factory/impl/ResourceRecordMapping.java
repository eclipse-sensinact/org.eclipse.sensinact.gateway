/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.IResourceMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.InvalidResourcePathException;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.VariableNotFoundException;

/**
 * Parses a resource path and associates it to a record path
 */
public class ResourceRecordMapping extends AbstractResourceMapping {

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
    public ResourceRecordMapping(final String rcPath, final RecordPath recordPath) throws InvalidResourcePathException {
        super(rcPath);
        this.recordPath = recordPath;
    }

    @Override
    protected IResourceMapping newInstance(final String path) throws InvalidResourcePathException {
        return new ResourceRecordMapping(path, recordPath);
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    /**
     * Returns the associated path in the record
     */
    public RecordPath getRecordPath() {
        return recordPath;
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
    public ResourceRecordMapping fillInVariables(final Map<String, String> variables)
            throws InvalidResourcePathException, VariableNotFoundException {

        RecordPath newRecordPath = recordPath.fillInVariables(variables);

        String newRcPath = getResourcePath();
        if (VariableSolver.containsVariables(newRcPath)) {
            newRcPath = VariableSolver.fillInVariables(newRcPath, variables);
        }

        return new ResourceRecordMapping(newRcPath, newRecordPath);
    }
}

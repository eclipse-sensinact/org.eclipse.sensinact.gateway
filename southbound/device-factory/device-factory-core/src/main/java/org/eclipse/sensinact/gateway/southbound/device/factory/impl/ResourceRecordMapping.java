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
     * Policies set on the entry, over the global ones
     */
    private final MappingPolicies policies;

    /**
     * Parses the resource path
     *
     * @param rcPath     Mapping resource path
     * @param recordPath Mapping record path
     * @throws InvalidResourcePathException Error parsing the resource path
     */
    public ResourceRecordMapping(final String rcPath, final RecordPath recordPath) throws InvalidResourcePathException {
        this(rcPath, recordPath, MappingPolicies.NONE);
    }

    /**
     * Parses the resource path
     *
     * @param rcPath     Mapping resource path
     * @param recordPath Mapping record path
     * @param policies   Policies set on the entry
     * @throws InvalidResourcePathException Error parsing the resource path
     */
    public ResourceRecordMapping(final String rcPath, final RecordPath recordPath, final MappingPolicies policies)
            throws InvalidResourcePathException {
        super(rcPath);
        this.recordPath = recordPath;
        this.policies = policies;
    }

    @Override
    protected IResourceMapping newInstance(final String path) throws InvalidResourcePathException {
        return new ResourceRecordMapping(path, recordPath, policies);
    }

    /**
     * Returns the policies of the entry
     */
    public MappingPolicies getPolicies() {
        return policies;
    }

    /**
     * Returns a copy of this mapping with other policies
     *
     * @param newPolicies Policies of the copy
     * @return The copy, or this mapping if the policies are the same
     * @throws InvalidResourcePathException Invalid resource path
     */
    public ResourceRecordMapping withPolicies(final MappingPolicies newPolicies) throws InvalidResourcePathException {
        if (policies.equals(newPolicies)) {
            return this;
        }
        return new ResourceRecordMapping(getResourcePath(), recordPath, newPolicies);
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

        return new ResourceRecordMapping(newRcPath, newRecordPath, policies);
    }
}

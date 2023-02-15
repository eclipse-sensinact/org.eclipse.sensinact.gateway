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
package org.eclipse.sensinact.gateway.southbound.device.factory;

import java.util.Map;

/**
 * Parses a resource path and associates it to a literal
 */
public class ResourceLiteralMapping extends AbstractResourceMapping {

    /**
     * Literal value type
     */
    private final ValueType valueType;

    /**
     * Associated value
     */
    private final Object value;

    /**
     * Parses the resource path
     *
     * @param rcPath    Mapping resource path
     * @param valueType Mapping literal value type
     * @param value     Mapping literal value
     * @throws InvalidResourcePathException Error parsing the resource path
     */
    public ResourceLiteralMapping(final String rcPath, final ValueType valueType, final Object value)
            throws InvalidResourcePathException {
        super(rcPath);
        this.valueType = valueType;
        this.value = value;
    }

    @Override
    protected IResourceMapping newInstance(final String path) throws InvalidResourcePathException {
        return new ResourceLiteralMapping(path, valueType, value);
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

    /**
     * Returns the literal value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the description of the value type
     */
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public String toString() {
        return String.format("%s[%s -> %s]", getClass().getSimpleName(), getResourcePath(), value);
    }

    /**
     * Returns a new instance with resolved variables
     *
     * @param variables Variables values
     * @throws InvalidResourcePathException Invalid resolved path
     * @throws VariableNotFoundException    Couldn't resolve a variable
     */
    public ResourceLiteralMapping fillInVariables(Map<String, String> variables)
            throws InvalidResourcePathException, VariableNotFoundException {

        final Object newValue;
        if (value instanceof String) {
            String strValue = (String) value;
            if (VariableSolver.containsVariables(strValue)) {
                newValue = VariableSolver.fillInVariables(strValue, variables);
            } else {
                newValue = value;
            }
        } else {
            newValue = value;
        }

        String newRcPath = getResourcePath();
        if (VariableSolver.containsVariables(newRcPath)) {
            newRcPath = VariableSolver.fillInVariables(newRcPath, variables);
        }

        return new ResourceLiteralMapping(newRcPath, valueType, newValue);
    }
}

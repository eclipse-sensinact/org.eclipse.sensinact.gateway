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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.impl.VariableSolver;

/**
 * Represents a record path
 */
public class RecordPath {

    /**
     * Creates a {@link RecordPath} according to the value of <code>rawPath</code>
     *
     * @param rawPath   Path as parsed
     * @param valueType Expected value type
     * @return A {@link RecordPath}
     */
    public static RecordPath fromObject(final Object rawPath, final ValueType valueType) {
        if (rawPath instanceof Number) {
            return new RecordPath((Number) rawPath, valueType);
        } else if (rawPath instanceof String) {
            return new RecordPath((String) rawPath, valueType);
        } else {
            throw new IllegalArgumentException("Not a string nor a number: " + rawPath);
        }
    }

    /**
     * Creates a {@link RecordPath} according to the value of <code>rawPath</code>
     *
     * @param rawPath Path as parsed
     * @return A {@link RecordPath}
     */
    public static RecordPath fromObject(final Object rawPath) {
        return fromObject(rawPath, ValueType.AS_IS);
    }

    /**
     * Record path string version
     */
    private final String strPath;

    /**
     * Record path integer value (if given as integer)
     */
    private final Integer intPath;

    /**
     * True if the path is an integer
     */
    private final boolean isInteger;

    /**
     * Value type
     */
    private final ValueType valueType;

    /**
     * True if the default value has been set
     */
    private boolean hasDefaultValue = false;

    /**
     * Default value to return if we don't find the resource value
     */
    private Object defaultValue = null;

    /**
     * Use for copy
     */
    private RecordPath(final RecordPath other) {
        this.defaultValue = other.defaultValue;
        this.hasDefaultValue = other.hasDefaultValue;
        this.intPath = other.intPath;
        this.isInteger = other.isInteger;
        this.strPath = other.strPath;
        this.valueType = other.valueType;
    }

    /**
     * @param path String path in the record
     */
    public RecordPath(final String path) {
        this(path, ValueType.AS_IS);
    }

    /**
     * @param path      String path in the record
     * @param valueType Expected value type
     */
    public RecordPath(final String path, final ValueType valueType) {
        this.valueType = valueType != null ? valueType : ValueType.AS_IS;
        this.isInteger = false;
        this.strPath = path;
        this.intPath = null;
    }

    /**
     * @param path Integer path in the record (column index, ...)
     */
    public RecordPath(final Number path) {
        this(path, ValueType.AS_IS);
    }

    /**
     * @param path      Integer path in the record (column index, ...)
     * @param valueType Expected value type
     */
    public RecordPath(final Number path, final ValueType valueType) {
        this.valueType = valueType != null ? valueType : ValueType.AS_IS;
        this.isInteger = true;
        this.intPath = path.intValue();
        this.strPath = path.toString();
    }

    /**
     * Makes a copy of the current path
     */
    private RecordPath copy() {
        return new RecordPath(this);
    }

    /**
     * Split the path into single-part record paths
     */
    public List<RecordPath> parts() {
        return Arrays.stream(asString().split("/")).map((part) -> new RecordPath(part, valueType))
                .collect(Collectors.toList());
    }

    /**
     * Returns this path as a string
     */
    public String asString() {
        return strPath;
    }

    /**
     * Returns this path as an integer
     *
     * @throws NumberFormatException Error parsing integer
     */
    public Integer asInt() {
        if (isInteger) {
            return intPath;
        }

        return Integer.valueOf(strPath);
    }

    /**
     * Returns true if the given path is an integer (considered as array index)
     */
    public boolean isInt() {
        return isInteger;
    }

    /**
     * Returns true if this record path has a default value
     */
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    /**
     * Returns the default value (check with {@link #hasDefaultValue} if it has
     * meaning
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value
     *
     * @param defaultValue the default value to set
     */
    public void setDefaultValue(final Object defaultValue) {
        this.hasDefaultValue = true;
        this.defaultValue = defaultValue;
    }

    /**
     * Removes the default value flag
     */
    public void unsetDefaultValue() {
        this.hasDefaultValue = false;
        this.defaultValue = null;
    }

    /**
     * Returns a new instance of the record path with resolved variables
     *
     * @param variables Resolved variables
     * @return The record path with replaced variables
     * @throws A path variable wasn't resolved
     */
    public RecordPath fillInVariables(final Map<String, String> variables) throws VariableNotFoundException {
        if (VariableSolver.containsVariables(strPath)) {
            return new RecordPath(VariableSolver.fillInVariables(strPath, variables), valueType);
        } else {
            return copy();
        }
    }

    /**
     * Converts the given path value to the excepted type
     *
     * @param value   Value to convert
     * @param options Mapping options
     * @return The read value in the excepted type
     * @throws ClassCastException Couldn't convert to the excepted type
     */
    public Object convertValue(final Object value, final DeviceMappingOptionsDTO options) throws ClassCastException {
        return valueType.convert(value, options);
    }

    @Override
    public String toString() {
        return asString();
    }
}

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a record path
 */
public class RecordPath {

    public static enum ValueType {
        /**
         * Represent available types
         */
        AS_IS("any", null), STRING("string", String::valueOf), INT("int", (v) -> Integer.valueOf(String.valueOf(v))),
        LONG("long", (v) -> Long.valueOf(String.valueOf(v))), FLOAT("float", (v) -> Float.valueOf(String.valueOf(v))),
        DOUBLE("double", (v) -> Double.valueOf(String.valueOf(v))),
        STRING_ARRAY("string[]", (v) -> asList(v, STRING.converter)),
        INT_ARRAY("int[]", (v) -> asList(v, INT.converter)), LONG_ARRAY("long[]", (v) -> asList(v, LONG.converter)),
        FLOAT_ARRAY("float[]", (v) -> asList(v, FLOAT.converter)),
        DOUBLE_ARRAY("double[]", (v) -> asList(v, DOUBLE.converter));

        /**
         * String representation
         */
        private final String repr;

        /**
         * Converter function
         */
        private final Function<Object, Object> converter;

        /**
         * Sets up the custom enumeration
         *
         * @param strRepr
         */
        ValueType(final String strRepr, final Function<Object, Object> converter) {
            this.repr = strRepr;
            this.converter = converter;
        }

        @Override
        public String toString() {
            return repr;
        }

        /**
         * Converts the given object to the right value type
         *
         * @param value Input value
         * @return Converted value
         */
        public Object convert(final Object value) {
            if (value == null) {
                return null;
            } else if (converter == null) {
                return value;
            } else {
                return converter.apply(value);
            }
        }

        /**
         * Converts input value to a list
         *
         * @param value         Input value
         * @param itemConverter Input array item converter
         * @return The converted list
         */
        private static Object asList(Object value, final Function<Object, Object> itemConverter) {
            Stream<?> stream;
            if (value instanceof String) {
                stream = Arrays.stream(((String) value).split(";|,"));
            } else if (value instanceof Collection) {
                stream = ((Collection<?>) value).stream();
            } else if (value.getClass().isArray()) {
                // Reflect array
                final int length = Array.getLength(value);
                final List<Object> arrayAsList = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    arrayAsList.add(Array.get(value, i));
                }
                stream = arrayAsList.stream();
            } else {
                // Maybe a single item
                return List.of(itemConverter.apply(value));
            }

            return stream.map(itemConverter).collect(Collectors.toList());
        }

        /**
         * Returns the {@link ValueType} matching the given string representation, or
         * null
         *
         * @param strName String representation of the value type
         * @return The type associated to the representation or null
         */
        public static ValueType fromString(final String strName) {
            if (strName == null || strName.isBlank()) {
                return AS_IS;
            }

            for (ValueType type : ValueType.values()) {
                if (type.repr.equals(strName)) {
                    return type;
                }
            }

            return AS_IS;
        }
    }

    /**
     * Record path string version
     */
    private String strPath;

    /**
     * Record path integer value (if given as integer)
     */
    private Integer intPath;

    /**
     * True if the path is an integer
     */
    private boolean isInteger;

    /**
     * Value type
     */
    private ValueType valueType = ValueType.AS_IS;

    /**
     * Use for copy
     */
    private RecordPath() {
    }

    /**
     * @param rawPath Path as parsed from mapping configuration
     */
    public RecordPath(final Object rawPath) throws IllegalArgumentException {
        this.valueType = ValueType.AS_IS;
        this.isInteger = false;
        loadPathType(rawPath, true);
    }

    /**
     * @param rawPath   Path as parsed from mapping configuration
     * @param valueType Excepted value type
     */
    public RecordPath(final Object rawPath, final String valueType) {
        this(rawPath, ValueType.fromString(valueType));
    }

    /**
     * @param rawPath   Path as parsed from mapping configuration
     * @param valueType Excepted value type
     */
    public RecordPath(final Object rawPath, final ValueType valueType) {
        this.isInteger = false;
        loadPathType(rawPath, true);
        if (valueType != null) {
            this.valueType = valueType;
        } else {
            this.valueType = ValueType.AS_IS;
        }
    }

    /**
     * Makes a copy of the current path
     */
    private RecordPath copy() {
        RecordPath path = new RecordPath();
        path.strPath = strPath;
        path.intPath = intPath;
        path.isInteger = isInteger;
        path.valueType = valueType;
        return path;
    }

    /**
     * Parses the path type
     *
     * @param rawPath  Path as read from the configuration
     * @param allowMap Allow parsing a map configuration (only at configuration
     *                 root)
     * @throws IllegalArgumentException Unsupported kind of path
     */
    private void loadPathType(final Object rawPath, boolean allowMap) throws IllegalArgumentException {
        if (rawPath instanceof RecordPath) {
            final RecordPath path = (RecordPath) rawPath;
            this.strPath = path.strPath;
            this.intPath = path.intPath;
            this.isInteger = path.isInteger;
            this.valueType = path.valueType;
        } else if (rawPath instanceof String) {
            // Simple string path
            this.strPath = (String) rawPath;
            this.isInteger = false;
        } else if (rawPath instanceof Number) {
            // Got an integer
            this.intPath = ((Number) rawPath).intValue();
            this.strPath = String.valueOf(this.intPath);
            this.isInteger = true;
        } else if (allowMap && rawPath instanceof Map) {
            // Complex configuration
            final Map<?, ?> pathDescription = (Map<?, ?>) rawPath;
            loadPathType(pathDescription.get("path"), false);

            Object valueType = pathDescription.get("type");
            if (valueType != null) {
                this.valueType = ValueType.fromString(String.valueOf(valueType));
            } else {
                this.valueType = ValueType.AS_IS;
            }
        } else {
            throw new IllegalArgumentException("Unsupported path type: " + rawPath + " / " + rawPath.getClass());
        }
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
     * @param value Value to convert
     * @return The read value in the excepted type
     * @throws ClassCastException Couldn't convert to the excepted type
     */
    public Object convertValue(Object value) throws ClassCastException {
        return valueType.convert(value);
    }

    @Override
    public String toString() {
        return asString();
    }
}

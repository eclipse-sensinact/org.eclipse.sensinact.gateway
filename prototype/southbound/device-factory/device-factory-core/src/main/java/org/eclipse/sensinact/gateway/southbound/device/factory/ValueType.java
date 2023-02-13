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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ValueType {
    /**
     * Represent available types
     */
    AS_IS("any", null),
    STRING("string", String::valueOf),
    INT("int", (v) -> Integer.valueOf(String.valueOf(v))),
    LONG("long", (v) -> Long.valueOf(String.valueOf(v))),
    FLOAT("float", (v) -> Float.valueOf(String.valueOf(v))),
    DOUBLE("double", (v) -> Double.valueOf(String.valueOf(v))),
    STRING_ARRAY("string[]", (v) -> asList(v, STRING.converter)),
    INT_ARRAY("int[]", (v) -> asList(v, INT.converter)),
    LONG_ARRAY("long[]", (v) -> asList(v, LONG.converter)),
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

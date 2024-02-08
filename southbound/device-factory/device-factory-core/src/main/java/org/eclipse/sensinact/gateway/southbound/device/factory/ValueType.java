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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;

public enum ValueType {
    /**
     * Represent available types
     */
    AS_IS("any", null, null), STRING("string", (v, o) -> String.valueOf(v), String.class), CHAR("char", (v, o) -> {
        if (v instanceof Character) {
            return (Character) v;
        } else if (v instanceof CharSequence) {
            final CharSequence cs = (CharSequence) v;
            if (cs.length() != 0) {
                return cs.charAt(0);
            } else {
                return null;
            }
        } else if (v instanceof Number) {
            int value = ((Number) v).intValue();
            if (value > Character.MAX_VALUE || value < Character.MIN_VALUE) {
                return null;
            } else {
                return (char) value;
            }
        } else {
            return null;
        }
    }, Character.class), BOOLEAN("boolean", (v, o) -> {
        if (v instanceof Boolean) {
            return (Boolean) v;
        } else if (v instanceof Number) {
            return ((Number) v).longValue() != 0;
        } else if (v instanceof CharSequence) {
            return Boolean.valueOf(v.toString());
        } else {
            return null;
        }
    }, Boolean.class), BYTE("byte", (v, o) -> {
        final Number parsed = parseNumber(v, true, o);
        return parsed != null ? parsed.byteValue() : null;
    }, Byte.class), SHORT("short", (v, o) -> {
        final Number parsed = parseNumber(v, true, o);
        return parsed != null ? parsed.shortValue() : null;
    }, Short.class), INT("int", (v, o) -> {
        final Number parsed = parseNumber(v, true, o);
        return parsed != null ? parsed.intValue() : null;
    }, Integer.class), LONG("long", (v, o) -> {
        final Number parsed = parseNumber(v, true, o);
        return parsed != null ? parsed.longValue() : null;
    }, Long.class), FLOAT("float", (v, o) -> {
        final Number parsed = parseNumber(v, false, o);
        return parsed != null ? parsed.floatValue() : null;
    }, Float.class), DOUBLE("double", (v, o) -> {
        final Number parsed = parseNumber(v, false, o);
        return parsed != null ? parsed.doubleValue() : null;
    }, Double.class), ANY_ARRAY("any[]", (v, o) -> asList(v, o, AS_IS), List.class),
    STRING_ARRAY("string[]", (v, o) -> asList(v, o, STRING), List.class),
    CHAR_ARRAY("char[]", (v, o) -> asList(v, o, CHAR), List.class),
    BOOLEAN_ARRAY("boolean[]", (v, o) -> asList(v, o, BOOLEAN), List.class),
    BYTE_ARRAY("byte[]", (v, o) -> asList(v, o, BYTE), List.class),
    SHORT_ARRAY("short[]", (v, o) -> asList(v, o, SHORT), List.class),
    INT_ARRAY("int[]", (v, o) -> asList(v, o, INT), List.class),
    LONG_ARRAY("long[]", (v, o) -> asList(v, o, LONG), List.class),
    FLOAT_ARRAY("float[]", (v, o) -> asList(v, o, FLOAT), List.class),
    DOUBLE_ARRAY("double[]", (v, o) -> asList(v, o, DOUBLE), List.class);

    /**
     * String representation
     */
    private final String repr;

    /**
     * Converter function
     */
    private final BiFunction<Object, DeviceMappingOptionsDTO, ?> converter;

    /**
     * Java class represented by the value type
     */
    private final Class<?> javaClass;

    /**
     * Try to find the number format matching the given locale
     *
     * @param options Mapping options
     * @return The number format for the given locale or null
     */
    private static NumberFormat getNumberFormat(final DeviceMappingOptionsDTO options, boolean integers) {
        final Locale locale = LocaleUtils.fromString(options.numbersLocale);
        if (locale == null) {
            return null;
        }

        if (integers) {
            return NumberFormat.getIntegerInstance(locale);
        } else {
            return NumberFormat.getNumberInstance(locale);
        }
    }

    /**
     * Parses an number
     *
     * @param value   Object value
     * @param options Mapping options (can define a locale)
     * @return The parsed long
     * @throws NumberFormatException Error parsing long
     */
    private static Number parseNumber(final Object value, final boolean expectInteger,
            final DeviceMappingOptionsDTO options) {
        if (value == null) {
            return null;
        }

        String strValue = String.valueOf(value).trim();
        if (strValue.isEmpty()) {
            return null;
        }

        switch (strValue.toLowerCase()) {
        case "nan":
            return expectInteger ? null : Double.NaN;

        case "inf":
        case "+inf":
            return expectInteger ? null : Double.POSITIVE_INFINITY;

        case "-inf":
            return expectInteger ? null : Double.NEGATIVE_INFINITY;

        default:
            break;
        }

        final NumberFormat format = getNumberFormat(options, expectInteger);
        if (format instanceof DecimalFormat) {
            final DecimalFormat decimalFormat = (DecimalFormat) format;
            strValue = strValue.replace("-", decimalFormat.getNegativePrefix());
            strValue = strValue.replace("+", decimalFormat.getPositivePrefix());
        }

        if (format != null) {
            try {
                return format.parse(strValue);
            } catch (ParseException e) {
                throw new NumberFormatException(String.format("Error parsing number '%s': %s", value, e.getMessage()));
            }
        } else if (expectInteger) {
            return Long.parseLong(strValue);
        } else {
            return Double.parseDouble(strValue);
        }
    }

    /**
     * Sets up the custom enumeration
     *
     * @param strRepr   String representation of the value type used in mapping
     *                  configuration
     * @param converter Function to convert any input to the value type
     * @param javaClass Java class represented by the value type
     */
    <T> ValueType(final String strRepr, final BiFunction<Object, DeviceMappingOptionsDTO, T> converter,
            final Class<T> javaClass) {
        this.repr = strRepr;
        this.converter = converter;
        this.javaClass = javaClass != null ? javaClass : Object.class;
    }

    @Override
    public String toString() {
        return repr;
    }

    /**
     * Returns the Java class associated to the value type
     */
    public Class<?> toJavaClass() {
        return this.javaClass;
    }

    /**
     * Converts the given object to the right value type
     *
     * @param value   Input value
     * @param options Mapping options
     * @return Converted value
     */
    public Object convert(final Object value, final DeviceMappingOptionsDTO options) {
        if (value == null) {
            return null;
        } else if (converter == null) {
            return value;
        } else {
            return converter.apply(value, options);
        }
    }

    /**
     * Converts input value to a list
     *
     * @param value    Input value
     * @param options  Mapping options
     * @param itemType List item type
     * @return The converted list
     */
    private static List<?> asList(final Object value, final DeviceMappingOptionsDTO options, final ValueType itemType) {
        Stream<?> stream;
        if (value instanceof String) {
            stream = Arrays.stream(((String) value).split(";|,")).map(String::trim);
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
            stream = Stream.of(value);
        }

        if (itemType.converter != null) {
            stream = stream.map(v -> itemType.converter.apply(v, options));
        }

        return stream.collect(Collectors.toUnmodifiableList());
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

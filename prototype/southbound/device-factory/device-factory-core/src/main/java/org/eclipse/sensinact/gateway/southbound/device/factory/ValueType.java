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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ValueType {
    /**
     * Represent available types
     */
    AS_IS("any", null),
    STRING("string", (v, o) -> String.valueOf(v)),
    INT("int", (v, o) -> {
        final Number parsed = parseNumber(v, true, o);
        return parsed != null ? parsed.intValue() : null;
    }),
    LONG("long", (v, o) -> {
        final Number parsed = parseNumber(v, true, o);
        return parsed != null ? parsed.longValue() : null;
    }),
    FLOAT("float", (v, o) -> {
        final Number parsed = parseNumber(v, false, o);
        return parsed != null ? parsed.floatValue() : null;
    }),
    DOUBLE("double", (v, o) -> {
        final Number parsed = parseNumber(v, false, o);
        return parsed != null ? parsed.doubleValue() : null;
    }),
    STRING_ARRAY("string[]", (v, o) -> asList(v, o, STRING.converter)),
    INT_ARRAY("int[]", (v, o) -> asList(v, o, INT.converter)),
    LONG_ARRAY("long[]", (v, o) -> asList(v, o, LONG.converter)),
    FLOAT_ARRAY("float[]", (v, o) -> asList(v, o, FLOAT.converter)),
    DOUBLE_ARRAY("double[]", (v, o) -> asList(v, o, DOUBLE.converter));

    private static final Logger logger = LoggerFactory.getLogger(ValueType.class);

    /**
     * String representation
     */
    private final String repr;

    /**
     * Converter function
     */
    private final BiFunction<Object, DeviceMappingOptionsDTO, Object> converter;

    /**
     * Try to find the number format matching the given locale
     *
     * @param options Mapping options
     * @return The number format for the given locale or null
     */
    private static NumberFormat getNumberFormat(final DeviceMappingOptionsDTO options, boolean integers) {
        final String strLocale = options.numbersLocale;
        if (strLocale == null || strLocale.isBlank()) {
            return null;
        }

        final String[] parts = strLocale.split("_");
        final Locale locale;
        switch (parts.length) {
        case 1:
            locale = new Locale(parts[0]);
            break;

        case 2:
            locale = new Locale(parts[0], parts[1]);
            break;

        case 3:
            locale = new Locale(parts[0], parts[1], parts[2]);
            break;

        default:
            logger.warn("Unhandled number locale {}", strLocale);
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
     * @param strRepr
     */
    ValueType(final String strRepr, final BiFunction<Object, DeviceMappingOptionsDTO, Object> converter) {
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
     * @param value         Input value
     * @param options       Mapping options
     * @param itemConverter Input array item converter
     * @return The converted list
     */
    private static Object asList(Object value, DeviceMappingOptionsDTO options,
            final BiFunction<Object, DeviceMappingOptionsDTO, Object> itemConverter) {
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
            return List.of(itemConverter.apply(value, options));
        }

        return stream.map(v -> itemConverter.apply(v, options)).collect(Collectors.toList());
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

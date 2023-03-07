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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.junit.jupiter.api.Test;

/**
 * Tests value type conversion
 */
public class ValueTypeTest {

    @Test
    void testNan() {
        final DeviceMappingOptionsDTO options = new DeviceMappingOptionsDTO();
        for (String nan : Arrays.asList("nan", "NaN")) {
            assertTrue(Double.isNaN((Float) ValueType.FLOAT.convert(nan, options)));
            assertTrue(Double.isNaN((Double) ValueType.DOUBLE.convert(nan, options)));
            assertNull(ValueType.INT.convert(nan, options));
            assertNull(ValueType.LONG.convert(nan, options));
        }

        for (String inf : Arrays.asList("inf", "Inf", "+inf", "+Inf", "-Inf", "-inf")) {
            assertTrue(Double.isInfinite((Float) ValueType.FLOAT.convert(inf, options)));
            assertTrue(Double.isInfinite((Double) ValueType.DOUBLE.convert(inf, options)));
            assertNull(ValueType.INT.convert(inf, options));
            assertNull(ValueType.LONG.convert(inf, options));
        }
    }

    @Test
    void testNumberLocale() {
        final DeviceMappingOptionsDTO options = new DeviceMappingOptionsDTO();

        final double doublePositiveValue = 3.14;
        final double doubleNegativeValue = -3.14;
        final float floatValue = (float) doublePositiveValue;

        // Default and root locale
        for (String locale : Arrays.asList(null, "")) {
            options.numbersLocale = locale;

            // Direct object
            assertEquals(doublePositiveValue, ValueType.AS_IS.convert(doublePositiveValue, options));
            assertEquals(floatValue, ValueType.AS_IS.convert(floatValue, options));
            assertEquals(floatValue, ValueType.FLOAT.convert(doublePositiveValue, options));
            assertEquals(doublePositiveValue, ValueType.DOUBLE.convert(doublePositiveValue, options));

            assertEquals(doubleNegativeValue, ValueType.AS_IS.convert(doubleNegativeValue, options));
            assertEquals(doubleNegativeValue, ValueType.DOUBLE.convert(doubleNegativeValue, options));

            // String representation
            String strValue = String.valueOf(doublePositiveValue);
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(floatValue, ValueType.FLOAT.convert(strValue, options));
            assertEquals(doublePositiveValue, ValueType.DOUBLE.convert(strValue, options));

            strValue = String.valueOf(doubleNegativeValue);
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(doubleNegativeValue, ValueType.DOUBLE.convert(strValue, options));
        }

        // US/UK locale
        for (String locale : Arrays.asList("en", "en_us", "en_uk")) {
            options.numbersLocale = locale;

            String strValue = "3.14";
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(floatValue, ValueType.FLOAT.convert(strValue, options));
            assertEquals(doublePositiveValue, ValueType.DOUBLE.convert(strValue, options));

            strValue = "-3.14";
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(doubleNegativeValue, ValueType.DOUBLE.convert(strValue, options));
        }

        // French locale
        for (String locale : Arrays.asList("fr", "fr_fr")) {
            options.numbersLocale = locale;

            String strValue = "3,14";
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(floatValue, ValueType.FLOAT.convert(strValue, options));
            assertEquals(doublePositiveValue, ValueType.DOUBLE.convert(strValue, options));

            strValue = "-3,14";
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(doubleNegativeValue, ValueType.DOUBLE.convert(strValue, options));
        }

        // Estonian locale: specific negative prefix
        for (String locale : Arrays.asList("et", "et_ee")) {
            options.numbersLocale = locale;

            String strValue = "3,14";
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(floatValue, ValueType.FLOAT.convert(strValue, options));
            assertEquals(doublePositiveValue, ValueType.DOUBLE.convert(strValue, options));

            strValue = "-3,14";
            assertEquals(strValue, ValueType.AS_IS.convert(strValue, options));
            assertEquals(doubleNegativeValue, ValueType.DOUBLE.convert(strValue, options));
        }
    }
}

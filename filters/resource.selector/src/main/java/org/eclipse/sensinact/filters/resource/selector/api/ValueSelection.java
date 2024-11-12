/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.filters.resource.selector.api;

import java.util.Collection;
import java.util.Map;

/**
 * Defines a selection based on a resource value
 *
 * Note that this is not suitable for location matching, where a
 * {@link LocationSelection} should be used instead
 */
public class ValueSelection {

    /**
     * The value to test against
     */
    public String value;

    /**
     * The type of test to use
     */
    public OperationType operation = OperationType.EQUALS;

    /**
     * If true then the result of the test will be negated
     */
    public boolean negate;

    /**
     * The type of the check, defaults to {@link CheckType#VALUE}
     */
    public CheckType checkType = CheckType.VALUE;

    /**
     * Test operation types.
     * <p>
     * If necessary type conversion will be used to convert
     * {@link ValueSelection#value} into the relevant type.
     *
     * <p>
     * If the resource value is a {@link Collection} then the test
     * will be applied against each value of the collection. If the
     * resource value is a {@link Map} then it will be applied against
     * each value in the {@link Map}.
     */
    public static enum OperationType {
        /**
         * The value of the resource must be equal to the supplied value.
         */
        EQUALS,
        /**
         * The value of the resource must be less than the supplied value.
         */
        LESS_THAN,
        /**
         * The value of the resource must be greater than the supplied value.
         */
        GREATER_THAN,
        /**
         * The value of the resource must be less than or equal to the supplied value.
         */
        LESS_THAN_OR_EQUAL,
        /**
         * The value of the resource must be greater than or equal to the supplied value.
         */
        GREATER_THAN_OR_EQUAL,
        /**
         * The value will be treated as a regular expression
         * which must match the entire resource value.
         * <p>
         * The resource value will be converted to a String for testing
         */
        REGEX,
        /**
         * The value will be treated as a regular expression
         * which must match some part of the resource value.
         * <p>
         * The resource value will be converted to a String for testing
         */
        REGEX_REGION,
        /**
         * The value and check type will be ignored, and only the presence of the value will be
         * checked, i.e. the value is non-null and not empty
         */
        IS_SET,
    }

    public static enum CheckType {
        /**
         * The test will be applied against resource value. For
         * arrays, collections and maps this test will be applied
         * against each element, and is a match if any value matches.
         */
        VALUE,
        /**
         * If true then the test will be applied against the size
         * of the resource value. For arrays, collections and maps
         * this is the number of elements. For Strings this is the
         * length of the string. For numbers it is the absolute
         * magnitude of the number.
         */
        SIZE,
        /**
         * If true then the test will be applied against the timestamp
         * of the resource value.
         */
        TIMESTAMP;
    }

    @Override
    public String toString() {
        return "ValueSelection [value=" + value + ", operation=" + operation + ", negate=" + negate + ", checkType="
                + checkType + "]";
    }
}

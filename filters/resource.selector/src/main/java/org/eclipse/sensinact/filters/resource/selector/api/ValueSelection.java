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

import org.eclipse.sensinact.filters.resource.selector.jackson.ValueSelectionDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Defines a selection based on a resource value
 *
 * Note that this is not suitable for location matching, where a
 * {@link LocationSelection} should be used instead
 */
@JsonDeserialize(using  = ValueSelectionDeserializer.class)
public record ValueSelection(
        /**
         * The value to test against
         */
        String value,
        /**
         * The type of test to use
         */
        OperationType operation,
        /**
         * If true then the result of the test will be negated, except that:
         *
         * <ul>
         *   <li>Checks that require a non null value (i.e. all except {@link OperationType#IS_SET}
         *   and {@link OperationType#IS_NOT_NULL}) will still require a non-null value, continuing
         *   to return false in this case</li>
         *   <li>Checks that require a set value (i.e. all except {@link OperationType#IS_SET} will
         *   still require a set resource, continuing to return false in this case
         * </ul>
         */
        boolean negate,
        /**
         * The type of the check, defaults to {@link CheckType#VALUE}
         */
        CheckType checkType
        ) {
    public ValueSelection {
        if(operation == null) {
            operation = OperationType.EQUALS;
        }
        if(checkType == null) {
            checkType = CheckType.VALUE;
        }
    }


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
         * The value and check type will be ignored, and only the presence of a value will be
         * checked, i.e. the resource has a timestamp. N.B. the value <em>may</em> be <code>null</code>
         */
        IS_SET,
        /**
         * The value and check type will be ignored, and the value will be compared with
         * <code>null</code>. N.B. the resource <em>must</em> be set to null. If it is
         * unset then this check will return <code>false</code>
         */
        IS_NOT_NULL,
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
}

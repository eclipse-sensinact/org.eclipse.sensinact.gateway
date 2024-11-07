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
package org.eclipse.sensinact.filters.ldap.antlr.impl;

import java.lang.reflect.Array;
import java.util.Collection;

import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;

/**
 * Comparison filter
 */
public class CriterionResourceOperator extends AbstractCriterion {

    /**
     * Tolerance on comparison of doubles
     */
    private static final double EPSILON = 0.0001;

    /**
     * Resource path
     */
    private final SensiNactPath rcPath;

    /**
     * Parsed expected value (can be {@link IStringValue}, {@link Boolean},
     * {@link Number}, ...)
     */
    private final Object expected;

    /**
     * LDAP comparator for this criterion
     */
    private final LdapComparator operator;

    /**
     * Expected value is numeric
     */
    private final boolean isNumericValue;

    /**
     * Expected value is boolean
     */
    private final boolean isBooleanValue;

    /**
     * Test presence instead of value
     */
    private final boolean isPresence;

    /**
     * @param rcPath   Resource path
     * @param value    Parsed expected value
     * @param operator LDAP comparison operator
     */
    public CriterionResourceOperator(final SensiNactPath rcPath, final Object value, final LdapComparator operator) {
        this(rcPath, value, operator, false);
    }

    private CriterionResourceOperator(final SensiNactPath rcPath, final Object value, final LdapComparator operator,
            boolean isNegative) {
        super(isNegative);
        this.rcPath = rcPath;
        this.expected = value;
        this.operator = operator;

        this.isNumericValue = this.expected instanceof Number;
        this.isBooleanValue = this.expected instanceof Boolean;
        this.isPresence = this.expected == Constants.ANY;
    }

    @Override
    public String toString() {
        String content = String.format("(%s%s%s)", rcPath, operator, expected);
        if (isNegative()) {
            content = String.format("(!%s)", content);
        }
        return content;
    }

    /**
     * Checks if the given {@link Number} matches the expected one
     *
     * @param otherNumber Resource value as a number
     * @return True if the resource matches the expected number
     */
    private boolean testNumericValue(final Number otherNumber) {
        final double testedValue = (otherNumber).doubleValue();
        final double expectedValue = ((Number) expected).doubleValue();

        switch (operator) {
        case EQUAL:
            return expectedValue == testedValue;

        case APPROX:
            return Math.abs(expectedValue - testedValue) < EPSILON;

        case GREATER_EQ:
            return testedValue >= expectedValue;

        case LESS_EQ:
            return testedValue <= expectedValue;

        default:
            // Shouldn't get there
            return false;
        }
    }

    /**
     * Checks if the given resource value matches the expected boolean.
     *
     * Compares {@link Boolean}s directly and considers <code>true</code> numbers
     * other than 0
     *
     * @param testedValue Resource value
     * @return True on match
     */
    private boolean testBooleanValue(final Object testedValue) {
        if (testedValue instanceof Boolean) {
            // Compare booleans
            return expected.equals(testedValue);
        } else if (testedValue instanceof Number) {
            // 0 is false
            return expected.equals(((Number) testedValue).doubleValue() != 0);
        }

        // Can't compare
        return false;
    }

    /**
     * Checks if the given resource value matches the expected string/pattern
     *
     * @param testedValue Resource value as a string
     * @return True on match
     */
    private boolean testStringValue(final String testedValue) {
        final String strTestedValue = testedValue;
        final IStringValue expectedValue;
        if (expected instanceof IStringValue) {
            expectedValue = (IStringValue) expected;
        } else {
            expectedValue = new PureString(String.valueOf(expected));
        }

        switch (operator) {
        case EQUAL:
            return expectedValue.matches(strTestedValue, false);

        case APPROX:
            return expectedValue.matches(strTestedValue.strip(), true);

        case GREATER_EQ:
            if (!expectedValue.isRegex()) {
                return strTestedValue.compareTo(expectedValue.getString()) >= 0;
            } else {
                return false;
            }

        case LESS_EQ:
            if (!expectedValue.isRegex()) {
                return strTestedValue.compareTo(expectedValue.getString()) <= 0;
            } else {
                return false;
            }

        default:
            // Shouldn't get there
            return false;
        }
    }

    /**
     * Walks the given array using {@link Array} and checks if any value matches the
     * expected one
     *
     * @param testedValue Array to walk in
     * @return True if any value/sub-value of the array matches
     */
    private boolean testInArray(final Object testedValue) {
        final int size = Array.getLength(testedValue);
        for (int i = 0; i < size; i++) {
            if (testValue(Array.get(testedValue, i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Walks the given collection and checks if any value matches the expected one
     *
     * @param testedCollection Collection to walk in
     * @return True if any value/sub-value of the collection matches
     */
    private boolean testInCollection(final Collection<?> testedCollection) {
        for (final Object object : testedCollection) {
            if (testValue(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if the given resource value matches the expected one
     *
     * @param rawValue Resource value
     * @return True on match
     */
    private boolean testValue(final Object rawValue) {
        if (rawValue == null) {
            // Check if we expected a null or false value
            return expected == null || Boolean.FALSE.equals(expected);
        } else if (expected == null) {
            // Not a null value but we expected one
            return false;
        } else if (isPresence) {
            // Value is not null, hence it's present
            return true;
        } else if (rawValue.getClass().isArray()) {
            return testInArray(rawValue);
        } else if (rawValue instanceof Collection) {
            return testInCollection((Collection<?>) rawValue);
        } else if (isNumericValue && rawValue instanceof Number) {
            return testNumericValue((Number) rawValue);
        } else if (isBooleanValue) {
            return testBooleanValue(rawValue);
        } else {
            return testStringValue(String.valueOf(rawValue));
        }
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        final boolean negative = isNegative();

        return (p, rs) -> rs.stream().anyMatch(r -> {
            if (!rcPath.accept(r)) {
                // Not the resource we're expecting
                return false;
            }

            boolean test;
            if (r.getValue() == null || r.getValue().getTimestamp() == null) {
                // Value is not set
                test = false;
            } else {
                test = testValue(r.getValue().getValue());
            }

            if (negative) {
                return !test;
            }
            return test;
        });
    }

    @Override
    public ILdapCriterion negate() {
        return new CriterionResourceOperator(rcPath, expected, operator, !isNegative());
    }
}

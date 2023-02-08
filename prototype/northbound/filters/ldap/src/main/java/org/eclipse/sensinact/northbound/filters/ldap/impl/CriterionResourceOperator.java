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
package org.eclipse.sensinact.northbound.filters.ldap.impl;

import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;

/**
 * Comparison filter
 */
public class CriterionResourceOperator extends AbstractCriterion {

    private static final double EPSILON = 0.0001;

    private final SensiNactPath rcPath;
    private final String expected;
    private final Double expectedDouble;
    private final LdapComparator operator;

    public CriterionResourceOperator(final SensiNactPath rcPath, final String value, final LdapComparator operator) {
        this.rcPath = rcPath;
        this.expected = value;

        Double converted;
        try {
            converted = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            converted = null;
        }

        this.expectedDouble = converted;
        this.operator = operator;
    }

    @Override
    public String toString() {
        String op;
        switch (operator) {
        case APPROX:
            op = "~=";
            break;

        case EQUAL:
            op = "=";
            break;

        case GREATER_EQ:
            op = ">=";
            break;

        case LESS_EQ:
            op = "<=";
            break;

        case PRESENT:
            op = "=*";
            break;

        default:
            op = "??";
            break;
        }

        String content = String.format("(%s%s%s)", rcPath, op, expected);
        if (isNegative()) {
            content = String.format("(!%s)", content);
        }
        return content;
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        final boolean negative = isNegative();

        if (operator == LdapComparator.PRESENT) {
            // Simplest operator
            return (p, rs) -> rs.stream().anyMatch(r -> rcPath.accept(r) && (r.getValue() != null || negative));
        }

        return (p, rs) -> rs.stream().anyMatch(r -> {
            if (!rcPath.accept(r)) {
                return false;
            }

            boolean test;
            if (r.getValue() == null || r.getValue().getTimestamp() == null) {
                test = false;
            } else {
                final Object rawValue = r.getValue().getValue();
                if (rawValue == null) {
                    test = expected.equals("null");
                } else if (expectedDouble != null && rawValue instanceof Number) {
                    final Number value = (Number) rawValue;
                    switch (operator) {
                    case EQUAL:
                        test = expectedDouble.doubleValue() == value.doubleValue();
                        break;

                    case APPROX:
                        test = Math.abs(expectedDouble.doubleValue() - value.doubleValue()) < EPSILON;
                        break;

                    case GREATER_EQ:
                        test = value.doubleValue() >= expectedDouble.doubleValue();
                        break;

                    case LESS_EQ:
                        test = value.doubleValue() <= expectedDouble.doubleValue();
                        break;

                    case PRESENT:
                        test = true;
                        break;

                    default:
                        // Shouldn't get there
                        test = false;
                        break;
                    }
                } else {
                    final String strValue = String.valueOf(rawValue);

                    switch (operator) {
                    case EQUAL:
                        test = expected.equals(strValue);
                        break;

                    case APPROX:
                        test = expected.strip().equalsIgnoreCase(strValue.strip());
                        break;

                    case GREATER_EQ:
                        test = strValue.compareTo(expected) >= 0;
                        break;

                    case LESS_EQ:
                        test = strValue.compareTo(expected) <= 0;
                        break;

                    case PRESENT:
                        test = true;
                        break;

                    default:
                        // Shouldn't get there
                        test = false;
                        break;
                    }
                }
            }

            if (negative) {
                return !test;
            }
            return test;
        });
    }
}

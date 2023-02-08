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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;

public class AnyMatch {

    public final List<Object> entries;
    public final BiFunction<Object, Object, Boolean> equalityPredicate;

    public AnyMatch(final Collection<Object> entries) {
        this(entries, Objects::equals);
    }

    public AnyMatch(final Collection<Object> entries, final BiFunction<Object, Object, Boolean> predicate) {
        this.entries = new ArrayList<>(entries);
        this.equalityPredicate = predicate;
    }

    @Override
    public boolean equals(final Object other) {
        return entries.stream().anyMatch(e -> equalityPredicate.apply(e, other));
    }

    @SuppressWarnings("unchecked")
    private boolean compare(final Object other, final Predicate<Integer> checker) {
        if (other instanceof Number) {
            final double otherDouble = ((Number) other).doubleValue();

            for (Object entry : entries) {
                if (entry instanceof Number) {
                    final Double entryDouble = ((Number) entry).doubleValue();
                    if (checker.test(entryDouble.compareTo(otherDouble))) {
                        return true;
                    }
                }
            }
        } else {
            for (Object entry : entries) {
                if (entry instanceof Comparable) {
                    Comparable<Object> entryCmp = (Comparable<Object>) entry;
                    if (checker.test(entryCmp.compareTo(other))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean lessThan(final Object other) {
        if (other == null) {
            return false;
        }

        return compare(other, c -> c < 0);
    }

    public boolean lessEqual(final Object other) {
        if (other == null) {
            return false;
        }

        return compare(other, c -> c <= 0);
    }

    public boolean greaterThan(final Object other) {
        if (other == null) {
            return false;
        }

        return compare(other, c -> c > 0);
    }

    public boolean greaterEqual(final Object other) {
        if (other == null) {
            return false;
        }

        return compare(other, c -> c >= 0);
    }

    public boolean compare(final Object other, final int comparatorRuleIndex) {
        return compare(other, comparatorRuleIndex, false);
    }

    public boolean compare(final Object otherValue, final int comparatorRuleIndex, final boolean reversed) {
        final Predicate<Object> comparator;

        switch (comparatorRuleIndex) {
        case ODataFilterParser.RULE_eqexpr:
            comparator = this::equals;
            break;

        case ODataFilterParser.RULE_neexpr:
            comparator = x -> !this.equals(x);
            break;

        case ODataFilterParser.RULE_ltexpr:
            if (!reversed) {
                comparator = this::lessThan;
            } else {
                comparator = this::greaterEqual;
            }
            break;

        case ODataFilterParser.RULE_leexpr:
            if (!reversed) {
                comparator = this::lessEqual;
            } else {
                comparator = this::greaterEqual;
            }
            break;

        case ODataFilterParser.RULE_gtexpr:
            if (!reversed) {
                comparator = this::greaterThan;
            } else {
                comparator = this::lessEqual;
            }
            break;

        case ODataFilterParser.RULE_geexpr:
            if (!reversed) {
                comparator = this::greaterEqual;
            } else {
                comparator = this::lessEqual;
            }
            break;

        case ODataFilterParser.RULE_hasexpr:
            // Uses an enumeration
            throw new UnsupportedRuleException("HAS not yet implemented");

        default:
            throw new UnsupportedRuleException("Unexpected comparison rule: " + comparatorRuleIndex);
        }

        return comparator.test(otherValue);
    }
}

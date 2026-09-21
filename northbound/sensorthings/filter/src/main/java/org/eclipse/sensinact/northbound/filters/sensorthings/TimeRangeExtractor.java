/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.filters.sensorthings;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterLexer;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolcommonexprContext;

/**
 * Reduces an OData $filter to history query constraints when the whole filter
 * is a conjunction of comparisons between one of the given fields and a
 * literal: time fields compared to datetime literals become a
 * {@link TimeRange}, value fields compared to numeric literals become a
 * {@link ValueFilter}. A present result means the constraints implement the
 * filter completely — a consumer pushing them into a history query has no
 * residual filter left to apply. Anything beyond that shape (or, not, ne,
 * other fields, functions, non-numeric value literals, field-on-the-right
 * comparisons) yields an empty result and the filter must be evaluated as
 * usual.
 */
public final class TimeRangeExtractor {

    /**
     * The fully-extracted filter: the time range (never null,
     * {@link TimeRange#ALL} when unconstrained) and the value filter, which
     * is {@code null} when the filter carries no value comparison.
     */
    public record Constraints(TimeRange range, ValueFilter valueFilter) {
    }

    private TimeRangeExtractor() {
    }

    public static Optional<TimeRange> extract(String filter, Set<String> timeFields) {
        return extractConstraints(filter, timeFields, Set.of()).map(Constraints::range);
    }

    public static Optional<Constraints> extractConstraints(String filter, Set<String> timeFields,
            Set<String> valueFields) {
        if (filter == null || filter.isBlank()) {
            return Optional.empty();
        }
        BoolcommonexprContext parsed;
        try {
            ODataFilterLexer lexer = new ODataFilterLexer(CharStreams.fromString(filter));
            ODataFilterParser parser = new ODataFilterParser(new CommonTokenStream(lexer));
            parser.setErrorHandler(new BailErrorStrategy());
            parsed = parser.boolcommonexpr();
        } catch (Exception e) {
            return Optional.empty();
        }

        Bounds bounds = new Bounds();
        if (!collect(parsed, timeFields, valueFields, bounds)) {
            return Optional.empty();
        }
        return Optional.of(bounds.toConstraints());
    }

    private static boolean collect(BoolcommonexprContext ctx, Set<String> timeFields, Set<String> valueFields,
            Bounds bounds) {
        if (ctx == null || ctx.orexpr() != null || ctx.notexpr() != null || ctx.isofexpr() != null
                || ctx.boolmethodcallexpr() != null) {
            return false;
        }

        boolean handled;
        if (ctx.boolparenexpr() != null) {
            handled = collect(ctx.boolparenexpr().boolcommonexpr(), timeFields, valueFields, bounds);
        } else if (ctx.commonexpr() != null) {
            handled = comparison(ctx, timeFields, valueFields, bounds);
        } else {
            handled = false;
        }
        if (!handled) {
            return false;
        }

        if (ctx.andexpr() != null) {
            return collect(ctx.andexpr().boolcommonexpr(), timeFields, valueFields, bounds);
        }
        return true;
    }

    private static boolean comparison(BoolcommonexprContext ctx, Set<String> timeFields, Set<String> valueFields,
            Bounds bounds) {
        String field = ctx.commonexpr().getText().trim();

        ParserRuleContext operand;
        ValueFilter.Op op;
        if (ctx.eqexpr() != null) {
            operand = ctx.eqexpr().commonexpr();
            op = ValueFilter.Op.EQ;
        } else if (ctx.ltexpr() != null) {
            operand = ctx.ltexpr().commonexpr();
            op = ValueFilter.Op.LT;
        } else if (ctx.leexpr() != null) {
            operand = ctx.leexpr().commonexpr();
            op = ValueFilter.Op.LE;
        } else if (ctx.gtexpr() != null) {
            operand = ctx.gtexpr().commonexpr();
            op = ValueFilter.Op.GT;
        } else if (ctx.geexpr() != null) {
            operand = ctx.geexpr().commonexpr();
            op = ValueFilter.Op.GE;
        } else {
            // ne (excludes non-numeric values inconsistently across
            // backends), has or a bare boolean expression
            return false;
        }
        String literal = operand.getText().trim();

        if (timeFields.contains(field)) {
            return timeBound(ctx, literal, bounds);
        }
        if (valueFields.contains(field)) {
            return valueCondition(op, literal, bounds);
        }
        return false;
    }

    private static boolean timeBound(BoolcommonexprContext ctx, String literal, Bounds bounds) {
        Instant time;
        try {
            time = OffsetDateTime.parse(literal).toInstant();
        } catch (DateTimeParseException e) {
            return false;
        }

        if (ctx.eqexpr() != null) {
            bounds.lower(time, true);
            bounds.upper(time, true);
        } else if (ctx.ltexpr() != null) {
            bounds.upper(time, false);
        } else if (ctx.leexpr() != null) {
            bounds.upper(time, true);
        } else if (ctx.gtexpr() != null) {
            bounds.lower(time, false);
        } else {
            bounds.lower(time, true);
        }
        return true;
    }

    /**
     * Only numeric literals: string and boolean comparisons have
     * backend-dependent semantics and stay with the in-memory evaluation
     */
    private static boolean valueCondition(ValueFilter.Op op, String literal, Bounds bounds) {
        BigDecimal number;
        try {
            number = new BigDecimal(literal);
        } catch (NumberFormatException e) {
            return false;
        }
        bounds.condition(new ValueFilter.Condition(op, number));
        return true;
    }

    /** Tightest intersection of the collected bounds and value conditions */
    private static class Bounds {

        private Instant from;
        private boolean fromInclusive = true;
        private Instant to;
        private boolean toInclusive = true;
        private final List<ValueFilter.Condition> conditions = new ArrayList<>();

        void lower(Instant time, boolean inclusive) {
            if (from == null || time.isAfter(from)) {
                from = time;
                fromInclusive = inclusive;
            } else if (time.equals(from)) {
                fromInclusive = fromInclusive && inclusive;
            }
        }

        void upper(Instant time, boolean inclusive) {
            if (to == null || time.isBefore(to)) {
                to = time;
                toInclusive = inclusive;
            } else if (time.equals(to)) {
                toInclusive = toInclusive && inclusive;
            }
        }

        void condition(ValueFilter.Condition condition) {
            conditions.add(condition);
        }

        Constraints toConstraints() {
            TimeRange range;
            if (from != null && to != null && from.isAfter(to)) {
                // contradictory bounds: a canonical range matching nothing
                range = new TimeRange(to, false, to, false);
            } else {
                range = new TimeRange(from, fromInclusive, to, toInclusive);
            }
            return new Constraints(range, conditions.isEmpty() ? null : new ValueFilter(conditions));
        }
    }
}

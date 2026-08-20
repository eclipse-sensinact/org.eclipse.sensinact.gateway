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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterLexer;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolcommonexprContext;

/**
 * Reduces an OData $filter to a {@link TimeRange} when the whole filter is a
 * conjunction of comparisons between one of the given time fields and a
 * datetime literal. A present result means the range implements the filter
 * completely — a consumer pushing it into a history query has no residual
 * filter left to apply. Anything beyond that shape (or, not, other fields,
 * functions, field-on-the-right comparisons) yields an empty result and the
 * filter must be evaluated as usual.
 */
public final class TimeRangeExtractor {

    private TimeRangeExtractor() {
    }

    public static Optional<TimeRange> extract(String filter, Set<String> timeFields) {
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
        if (!collect(parsed, timeFields, bounds)) {
            return Optional.empty();
        }
        return Optional.of(bounds.toTimeRange());
    }

    private static boolean collect(BoolcommonexprContext ctx, Set<String> timeFields, Bounds bounds) {
        if (ctx == null || ctx.orexpr() != null || ctx.notexpr() != null || ctx.isofexpr() != null
                || ctx.boolmethodcallexpr() != null) {
            return false;
        }

        boolean handled;
        if (ctx.boolparenexpr() != null) {
            handled = collect(ctx.boolparenexpr().boolcommonexpr(), timeFields, bounds);
        } else if (ctx.commonexpr() != null) {
            handled = comparison(ctx, timeFields, bounds);
        } else {
            handled = false;
        }
        if (!handled) {
            return false;
        }

        if (ctx.andexpr() != null) {
            return collect(ctx.andexpr().boolcommonexpr(), timeFields, bounds);
        }
        return true;
    }

    private static boolean comparison(BoolcommonexprContext ctx, Set<String> timeFields, Bounds bounds) {
        String field = ctx.commonexpr().getText().trim();
        if (!timeFields.contains(field)) {
            return false;
        }

        ParserRuleContext operand;
        if (ctx.eqexpr() != null) {
            operand = ctx.eqexpr().commonexpr();
        } else if (ctx.ltexpr() != null) {
            operand = ctx.ltexpr().commonexpr();
        } else if (ctx.leexpr() != null) {
            operand = ctx.leexpr().commonexpr();
        } else if (ctx.gtexpr() != null) {
            operand = ctx.gtexpr().commonexpr();
        } else if (ctx.geexpr() != null) {
            operand = ctx.geexpr().commonexpr();
        } else {
            // ne, has or a bare boolean expression
            return false;
        }

        Instant time;
        try {
            time = OffsetDateTime.parse(operand.getText().trim()).toInstant();
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

    /** Tightest intersection of the collected bounds */
    private static class Bounds {

        private Instant from;
        private boolean fromInclusive = true;
        private Instant to;
        private boolean toInclusive = true;

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

        TimeRange toTimeRange() {
            if (from != null && to != null && from.isAfter(to)) {
                // contradictory bounds: a canonical range matching nothing
                return new TimeRange(to, false, to, false);
            }
            return new TimeRange(from, fromInclusive, to, toInclusive);
        }
    }
}

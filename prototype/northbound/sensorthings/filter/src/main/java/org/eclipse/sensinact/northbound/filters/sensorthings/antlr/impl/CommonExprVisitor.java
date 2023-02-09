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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.AddexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.CommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DivexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.FirstmemberexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.MethodcallexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.ModexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.MulexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.NegateexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.ParenexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.PrimitiveliteralContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.SubexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.PathHandler;
import org.locationtech.spatial4j.shape.Shape;

public class CommonExprVisitor extends ODataFilterBaseVisitor<Function<ResourceValueFilterInputHolder, Object>> {

    private final Parser parser;

    private final GeoGeographyVisitor visitor;

    public CommonExprVisitor(Parser parser) {
        this.parser = parser;
        visitor = new GeoGeographyVisitor(parser);
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitPrimitiveliteral(PrimitiveliteralContext ctx) {
        ParserRuleContext element = ctx.getChild(ParserRuleContext.class, 0);
        switch (element.getRuleIndex()) {
        case ODataFilterParser.RULE_nullvalue:
            return x -> null;

        case ODataFilterParser.RULE_booleanvalue:
            return x -> ("true".equalsIgnoreCase(ctx.getText()));

        case ODataFilterParser.RULE_decimalvalue:
        case ODataFilterParser.RULE_doublevalue: {
            final Double value = new DoubleVisitor().visitChildren(ctx);
            return x -> value;
        }
        case ODataFilterParser.RULE_sbytevalue:
        case ODataFilterParser.RULE_bytevalue:
        case ODataFilterParser.RULE_int16value:
        case ODataFilterParser.RULE_int32value:
        case ODataFilterParser.RULE_int64value: {
            final Integer value = new IntegerVisitor().visitChildren(ctx);
            return x -> value;
        }

        case ODataFilterParser.RULE_string_1: {
            final String value = new StringVisitor().visitChildren(ctx);
            return x -> value;
        }

        case ODataFilterParser.RULE_datetimeoffsetvalue: {
            final OffsetDateTime value = DateTimeVisitors.dateTimeOffset(element);
            return x -> value;
        }

        case ODataFilterParser.RULE_datevalue: {
            final LocalDate value = DateTimeVisitors.date(element);
            return x -> value;
        }

        case ODataFilterParser.RULE_timeofdayvalue: {
            final LocalTime value = DateTimeVisitors.timeOfDay(element);
            return x -> value;
        }

        case ODataFilterParser.RULE_duration: {
            final Duration value = DateTimeVisitors.duration(element);
            return x -> value;
        }

        case ODataFilterParser.RULE_geographycollection:
        case ODataFilterParser.RULE_geographylinestring:
        case ODataFilterParser.RULE_geographymultilinestring:
        case ODataFilterParser.RULE_geographymultipoint:
        case ODataFilterParser.RULE_geographymultipolygon:
        case ODataFilterParser.RULE_geographypolygon:
        case ODataFilterParser.RULE_geographypoint: {
            final Shape value = visitor.visit(element);
            return x -> value;
        }

        default:
            throw new UnsupportedRuleException(parser, element);
        }
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitNegateexpr(NegateexprContext ctx) {
        Function<ResourceValueFilterInputHolder, Object> subExpr = visitCommonexpr(ctx.commonexpr());
        return x -> {
            Object res = subExpr.apply(x);
            if (res instanceof Number) {
                if (res instanceof Integer) {
                    return -((Integer) res);
                } else {
                    return -((Number) res).doubleValue();
                }
            }

            throw new InvalidResultTypeException("Can't negate", "number", res);
        };
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitParenexpr(ParenexprContext ctx) {
        return visitCommonexpr(ctx.commonexpr());
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitMethodcallexpr(MethodcallexprContext ctx) {
        return new MethodCallExprVisitor(parser).visit(ctx);
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitFirstmemberexpr(FirstmemberexprContext ctx) {
        return new PathHandler(ctx.getText())::handle;
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitCommonexpr(CommonexprContext ctx) {
        final Function<ResourceValueFilterInputHolder, Object> firstExpr = visit(ctx.getChild(0));
        if (ctx.getChildCount() == 1) {
            // Simple expression
            return firstExpr;
        }

        // We have an operator and a right part
        final ParserRuleContext rightElement = ctx.getChild(ParserRuleContext.class, 1);
        final CommonExprVisitor rightVisitor = new CommonExprVisitor(parser);

        final BiFunction<Object, Object, Object> subOperation;
        final Function<ResourceValueFilterInputHolder, Object> rightExpr;

        switch (rightElement.getRuleIndex()) {
        case ODataFilterParser.RULE_addexpr:
            rightExpr = rightVisitor.visit(((AddexprContext) rightElement).commonexpr());
            subOperation = this::add;
            break;

        case ODataFilterParser.RULE_subexpr:
            rightExpr = rightVisitor.visit(((SubexprContext) rightElement).commonexpr());
            subOperation = this::sub;
            break;

        case ODataFilterParser.RULE_mulexpr:
            rightExpr = rightVisitor.visit(((MulexprContext) rightElement).commonexpr());
            subOperation = this::mul;
            break;

        case ODataFilterParser.RULE_divexpr:
            rightExpr = rightVisitor.visit(((DivexprContext) rightElement).commonexpr());
            subOperation = this::div;
            break;

        case ODataFilterParser.RULE_modexpr:
            rightExpr = rightVisitor.visit(((ModexprContext) rightElement).commonexpr());
            subOperation = this::mod;
            break;

        default:
            throw new UnsupportedRuleException("Unexpected right operator", parser, rightElement);
        }

        return x -> {
            final Object leftValue = firstExpr.apply(x);
            final Object rightValue = rightExpr.apply(x);
            return subOperation.apply(leftValue, rightValue);
        };
    }

    private Object add(Object l, Object r) {
        if (l instanceof Number && r instanceof Number) {
            if (l instanceof Integer && r instanceof Integer) {
                return ((Integer) l) + ((Integer) r);
            } else {
                return ((Number) l).doubleValue() + ((Number) r).doubleValue();
            }
        } else if (l instanceof String || r instanceof String) {
            return String.valueOf(l) + String.valueOf(r);
        } else if (r instanceof Duration) {
            final Duration duration = (Duration) r;
            if (l instanceof Temporal) {
                return duration.addTo((Temporal) l);
            }
        }

        throw new InvalidResultTypeException("Can't add", "numbers", l, r);
    }

    private Object sub(Object l, Object r) {
        if (l instanceof Number && r instanceof Number) {
            if (l instanceof Integer && r instanceof Integer) {
                return ((Integer) l) - ((Integer) r);
            } else {
                return ((Number) l).doubleValue() - ((Number) r).doubleValue();
            }
        } else if (r instanceof Duration) {
            final Duration duration = (Duration) r;
            if (l instanceof Temporal) {
                return duration.negated().addTo((Temporal) l);
            }
        } else if (l instanceof Temporal && r instanceof Temporal) {
            // Date A - Date B => Duration between B and A
            return Duration.between((Temporal) r, (Temporal) l);
        }
        throw new InvalidResultTypeException("Can't substract", "numbers", l, r);
    }

    private Object mul(Object l, Object r) {
        if (l instanceof Number && r instanceof Number) {
            if (l instanceof Integer && r instanceof Integer) {
                return ((Integer) l) * ((Integer) r);
            } else {
                return ((Number) l).doubleValue() * ((Number) r).doubleValue();
            }
        } else {
            throw new InvalidResultTypeException("Can't multiply", "numbers", l, r);
        }
    }

    private Object div(Object l, Object r) {
        if (l instanceof Number && r instanceof Number) {
            return ((Number) l).doubleValue() / ((Number) r).doubleValue();
        } else {
            throw new InvalidResultTypeException("Can't divide", "numbers", l, r);
        }
    }

    private Object mod(Object l, Object r) {
        if (l instanceof Integer && r instanceof Integer) {
            return ((Integer) l) % ((Integer) r);
        } else {
            throw new InvalidResultTypeException("Can't apply modulus", "integers", l, r);
        }
    }
}

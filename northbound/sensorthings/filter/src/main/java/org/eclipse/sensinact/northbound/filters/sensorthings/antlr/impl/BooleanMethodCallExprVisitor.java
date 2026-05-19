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

import static org.eclipse.sensinact.gateway.filters.esri.geometry.EsriUtils.WGS84_COORDS;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolmethodcallexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.CommonexprContext;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;

public class BooleanMethodCallExprVisitor
        extends AbstractGeometryConvertingVisitor<Function<ResourceValueFilterInputHolder, Boolean>> {

    final Parser parser;
    final CommonExprVisitor visitor;

    public BooleanMethodCallExprVisitor(Parser parser) {
        this.parser = parser;
        this.visitor = new CommonExprVisitor(parser);
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Boolean> visitBoolmethodcallexpr(BoolmethodcallexprContext ctx) {
        final ParserRuleContext child = ctx.getChild(ParserRuleContext.class, 0);
        switch (child.getRuleIndex()) {
        case ODataFilterParser.RULE_containsmethodcallexpr:
        case ODataFilterParser.RULE_startswithmethodcallexpr:
        case ODataFilterParser.RULE_endswithmethodcallexpr:
            // 2 string arguments
            return runDualString(child);

        case ODataFilterParser.RULE_intersectsmethodcallexpr:
        case ODataFilterParser.RULE_stintersectsmethodcallexpr:
            return runGeoRelation(child, (l, r) -> !GeometryEngine.disjoint(l, r, WGS84_COORDS));

        case ODataFilterParser.RULE_stcrossesmethodcallexpr:
            return runGeoRelation(child, (l, r) -> GeometryEngine.crosses(l, r, WGS84_COORDS));

        case ODataFilterParser.RULE_stdisjointmethodcallexpr:
            return runGeoRelation(child, (l, r) -> GeometryEngine.disjoint(l, r, WGS84_COORDS));

        case ODataFilterParser.RULE_stcontainssmethodcallexpr:
            return runGeoRelation(child, (l, r) -> GeometryEngine.contains(l, r, WGS84_COORDS));

        case ODataFilterParser.RULE_stwithinmethodcallexpr:
            return runGeoRelation(child, (l, r) -> GeometryEngine.within(l, r, WGS84_COORDS));

        case ODataFilterParser.RULE_stequalsmethodcallexpr:
            return runGeoRelation(child, (l, r) -> GeometryEngine.equals(l, r, WGS84_COORDS));

        case ODataFilterParser.RULE_strelatesmethodcallexpr:
            return runSpatialRelates(child);

        case ODataFilterParser.RULE_stoverlapsmethodcallexpr:
            return runGeoRelation(child, (l, r) -> GeometryEngine.overlaps(l, r, WGS84_COORDS));

        case ODataFilterParser.RULE_sttouchesmethodcallexpr:
            return runGeoRelation(child, (l, r) -> GeometryEngine.touches(l, r, WGS84_COORDS));

        default:
            throw new UnsupportedRuleException("Unsupported method call", parser, child);
        }
    }

    private Function<ResourceValueFilterInputHolder, Boolean> runDualString(ParserRuleContext ctx) {
        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);
        final BiFunction<String, String, Boolean> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_containsmethodcallexpr:
            operation = (l, r) -> l.contains(r);
            break;

        case ODataFilterParser.RULE_startswithmethodcallexpr:
            operation = (l, r) -> l.startsWith(r);
            break;

        case ODataFilterParser.RULE_endswithmethodcallexpr:
            operation = (l, r) -> l.endsWith(r);
            break;

        default:
            throw new UnsupportedRuleException("Unsupported method call", parser, ctx);
        }

        return x -> {
            String left = convert(ctx, leftFun.apply(x), String.class, false, "arg1");
            String right = convert(ctx, rightFun.apply(x), String.class, false, "arg2");
            return operation.apply(left, right);
        };
    }

    private Function<ResourceValueFilterInputHolder, Boolean> runGeoRelation(ParserRuleContext ctx,
            BiPredicate<Geometry, Geometry> predicate) {
        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);

        return x -> {
            Geometry left = convert(ctx, leftFun.apply(x), Geometry.class, false, "arg1");
            Geometry right = convert(ctx, rightFun.apply(x), Geometry.class, false, "arg2");
            return predicate.test(left, right);
        };
    }

    private Function<ResourceValueFilterInputHolder, Boolean> runSpatialRelates(ParserRuleContext ctx) {
        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);
        final CommonexprContext relationExpr = ctx.getChild(CommonexprContext.class, 2);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);
        final Function<ResourceValueFilterInputHolder, Object> relationFun = visitor.visitCommonexpr(relationExpr);

        return x -> {
            Geometry left = convert(ctx, leftFun.apply(x), Geometry.class, false, "arg1");
            Geometry right = convert(ctx, rightFun.apply(x), Geometry.class, false, "arg2");
            String strRelation = convert(ctx, relationFun.apply(x), String.class, false, "arg3");

            return GeometryEngine.relate(left, right, WGS84_COORDS, strRelation);
        };
    }
}

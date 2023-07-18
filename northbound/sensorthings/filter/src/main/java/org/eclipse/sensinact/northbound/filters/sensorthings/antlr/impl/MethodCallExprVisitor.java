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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.GeoJsonType;
import org.eclipse.sensinact.gateway.geojson.LineString;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.CommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.MethodcallexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.SubstringmethodcallexprContext;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceCalculator;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.ShapeFactory;

public class MethodCallExprVisitor extends ODataFilterBaseVisitor<Function<ResourceValueFilterInputHolder, Object>> {

    final Parser parser;
    final CommonExprVisitor visitor;

    public MethodCallExprVisitor(Parser parser) {
        this.parser = parser;
        this.visitor = new CommonExprVisitor(parser);
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitMethodcallexpr(MethodcallexprContext ctx) {
        final ParserRuleContext child = ctx.getChild(ParserRuleContext.class, 0);
        switch (child.getRuleIndex()) {
        // String operations
        case ODataFilterParser.RULE_tolowermethodcallexpr:
        case ODataFilterParser.RULE_touppermethodcallexpr:
        case ODataFilterParser.RULE_lengthmethodcallexpr:
        case ODataFilterParser.RULE_trimmethodcallexpr:
            // 1 string argument
            return runSingleString(child);

        case ODataFilterParser.RULE_concatmethodcallexpr:
        case ODataFilterParser.RULE_indexofmethodcallexpr:
            // 2 string arguments
            return runDualString(child);

        case ODataFilterParser.RULE_substringmethodcallexpr:
            // 2 or 3 arguments
            return runSubstring((SubstringmethodcallexprContext) child);

        // Date operations
        case ODataFilterParser.RULE_yearmethodcallexpr:
        case ODataFilterParser.RULE_monthmethodcallexpr:
        case ODataFilterParser.RULE_daymethodcallexpr:
        case ODataFilterParser.RULE_hourmethodcallexpr:
        case ODataFilterParser.RULE_minutemethodcallexpr:
        case ODataFilterParser.RULE_secondmethodcallexpr:
        case ODataFilterParser.RULE_fractionalsecondsmethodcallexpr:
        case ODataFilterParser.RULE_datemethodcallexpr:
        case ODataFilterParser.RULE_timemethodcallexpr:
        case ODataFilterParser.RULE_totaloffsetminutesmethodcallexpr:
            return runDateMethod(child);

        case ODataFilterParser.RULE_mindatetimemethodcallexpr:
            return x -> Instant.EPOCH;

        case ODataFilterParser.RULE_maxdatetimemethodcallexpr:
            return x -> Instant.MAX;

        case ODataFilterParser.RULE_nowmethodcallexpr:
            final Instant parseTime = Instant.now();
            return x -> parseTime;

        // Math operations
        case ODataFilterParser.RULE_roundmethodcallexpr:
        case ODataFilterParser.RULE_floormethodcallexpr:
        case ODataFilterParser.RULE_ceilingmethodcallexpr:
            return runMathOps(child);

        // Spatial operations
        case ODataFilterParser.RULE_geolengthmethodcallexpr:
            return runGeoLength(child);
        case ODataFilterParser.RULE_distancemethodcallexpr:
            return runGeoDistance(child);

        default:
            throw new UnsupportedRuleException("Unsupported method call", parser, child);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(final ParserRuleContext ctx, final Object object, final Class<T> type,
            final boolean allowNull, final String placeDescription) {
        if (object == null) {
            if (allowNull) {
                return null;
            } else {
                throw new InvalidResultTypeException(placeDescription + " is null", type.getSimpleName(), object);
            }
        }

        if (!type.isAssignableFrom(object.getClass())) {
            throw new InvalidResultTypeException("Unsupported " + placeDescription + " for \"" + ctx.getText() + "\"",
                    type.getSimpleName(), object);
        }

        return (T) object;
    }

    private Function<ResourceValueFilterInputHolder, Object> runSingleString(ParserRuleContext ctx) {
        final CommonexprContext subExpr = ctx.getChild(CommonexprContext.class, 0);
        final Function<ResourceValueFilterInputHolder, Object> targetExpr = visitor.visitCommonexpr(subExpr);
        final Function<String, Object> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_tolowermethodcallexpr:
            operation = s -> s.toLowerCase();
            break;

        case ODataFilterParser.RULE_touppermethodcallexpr:
            operation = s -> s.toUpperCase();
            break;

        case ODataFilterParser.RULE_lengthmethodcallexpr:
            operation = s -> s.length();
            break;

        case ODataFilterParser.RULE_trimmethodcallexpr:
            operation = s -> s.trim();
            break;

        default:
            throw new RuntimeException("Unsupported rule: " + parser.getRuleNames()[ctx.getRuleIndex()]);
        }

        return x -> {
            Object res = targetExpr.apply(x);
            if (res instanceof String) {
                return operation.apply((String) res);
            }

            throw new InvalidResultTypeException("Error calling string method", "string", res);
        };
    }

    private Function<ResourceValueFilterInputHolder, Object> runDualString(ParserRuleContext ctx) {
        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);
        final BiFunction<String, String, Object> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_concatmethodcallexpr:
            operation = (l, r) -> l + r;
            break;

        case ODataFilterParser.RULE_indexofmethodcallexpr:
            operation = (l, r) -> l.indexOf(r);
            break;

        default:
            throw new UnsupportedRuleException(parser, ctx);
        }

        return x -> {
            String left = convert(ctx, leftFun.apply(x), String.class, false, "arg1");
            String right = convert(ctx, rightFun.apply(x), String.class, false, "arg2");
            return operation.apply((String) left, (String) right);
        };
    }

    private Function<ResourceValueFilterInputHolder, Object> runSubstring(SubstringmethodcallexprContext ctx) {
        final Function<ResourceValueFilterInputHolder, Object> stringFun = visitor.visitCommonexpr(ctx.commonexpr(0));
        final Function<ResourceValueFilterInputHolder, Object> startPosFun = visitor.visitCommonexpr(ctx.commonexpr(1));

        if (ctx.commonexpr().size() == 2) {
            // String + start
            return x -> {
                String string = convert(ctx, stringFun.apply(x), String.class, false, "string");
                Integer startPos = convert(ctx, startPosFun.apply(x), Integer.class, false, "start");
                return string.substring(startPos);
            };
        } else {
            // String + start + length
            final Function<ResourceValueFilterInputHolder, Object> lengthFun = visitor
                    .visitCommonexpr(ctx.commonexpr(2));
            return x -> {
                String string = convert(ctx, stringFun.apply(x), String.class, false, "string");
                Integer startPos = convert(ctx, startPosFun.apply(x), Integer.class, false, "start");
                Integer length = convert(ctx, lengthFun.apply(x), Integer.class, false, "length");
                int endPos = startPos + length;
                return string.substring(startPos, endPos);
            };
        }
    }

    private Function<ResourceValueFilterInputHolder, Object> runDateMethod(final ParserRuleContext ctx) {

        final CommonexprContext subExpr = ctx.getChild(CommonexprContext.class, 0);
        final Function<ResourceValueFilterInputHolder, Object> targetExpr = visitor.visitCommonexpr(subExpr);
        final Function<Temporal, Object> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_yearmethodcallexpr:
            operation = t -> t.get(ChronoField.YEAR);
            break;

        case ODataFilterParser.RULE_monthmethodcallexpr:
            operation = t -> t.get(ChronoField.MONTH_OF_YEAR);
            break;

        case ODataFilterParser.RULE_daymethodcallexpr:
            operation = t -> t.get(ChronoField.DAY_OF_MONTH);
            break;

        case ODataFilterParser.RULE_hourmethodcallexpr:
            operation = t -> t.get(ChronoField.HOUR_OF_DAY);
            break;

        case ODataFilterParser.RULE_minutemethodcallexpr:
            operation = t -> t.get(ChronoField.MINUTE_OF_HOUR);
            break;

        case ODataFilterParser.RULE_secondmethodcallexpr:
            operation = t -> t.get(ChronoField.SECOND_OF_MINUTE);
            break;

        case ODataFilterParser.RULE_fractionalsecondsmethodcallexpr:
            operation = t -> t.get(ChronoField.NANO_OF_SECOND);
            break;

        case ODataFilterParser.RULE_datemethodcallexpr:
            operation = o -> {
                if (o instanceof Instant) {
                    return ((Instant) o).atOffset(ZoneOffset.UTC).toLocalDate();
                } else {
                    return (LocalDate) o;
                }
            };
            break;

        case ODataFilterParser.RULE_timemethodcallexpr:
            operation = t -> {
                if (t instanceof OffsetDateTime) {
                    return ((OffsetDateTime) t).toLocalTime();
                } else if (t instanceof LocalTime) {
                    return (LocalTime) t;
                } else if (t instanceof Instant) {
                    return OffsetDateTime.ofInstant((Instant) t, ZoneOffset.UTC).toLocalTime();
                } else {
                    throw new InvalidResultTypeException("Can't extract time", "datetime, time or instant", t);
                }
            };
            break;

        case ODataFilterParser.RULE_totaloffsetminutesmethodcallexpr:
            operation = t -> {
                if (t instanceof OffsetDateTime) {
                    return ((OffsetDateTime) t).getOffset().getTotalSeconds() / 60;
                } else {
                    return 0;
                }
            };
            break;

        default:
            throw new UnsupportedRuleException("Unsupported date method call", parser, ctx);
        }

        return x -> {
            Object res = targetExpr.apply(x);
            if (res instanceof Temporal) {
                return operation.apply((Temporal) res);
            }

            throw new InvalidResultTypeException("Can't execute date method", "temporal", res);
        };
    }

    private Function<ResourceValueFilterInputHolder, Object> runMathOps(ParserRuleContext ctx) {
        final CommonexprContext subExpr = ctx.getChild(CommonexprContext.class, 0);
        final Function<ResourceValueFilterInputHolder, Object> targetExpr = visitor.visitCommonexpr(subExpr);
        final Function<Number, Integer> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_roundmethodcallexpr:
            operation = n -> (int) Math.round(n.doubleValue());
            break;
        case ODataFilterParser.RULE_floormethodcallexpr:
            operation = n -> (int) Math.floor(n.doubleValue());
            break;
        case ODataFilterParser.RULE_ceilingmethodcallexpr:
            operation = n -> (int) Math.ceil(n.doubleValue());
            break;

        default:
            throw new RuntimeException("Unsupported math method: " + parser.getRuleNames()[ctx.getRuleIndex()]);
        }

        return x -> {
            Number res = convert(ctx, targetExpr.apply(x), Number.class, false, "number");
            return operation.apply(res);
        };
    }

    private Function<ResourceValueFilterInputHolder, Object> runGeoLength(ParserRuleContext ctx) {
        final SpatialContext spatialContext = SpatialContext.GEO;

        final CommonexprContext subExpr = ctx.getChild(CommonexprContext.class, 0);
        final Function<ResourceValueFilterInputHolder, Object> exprFun = visitor.visitCommonexpr(subExpr);

        return x -> {
            GeoJsonObject obj = convert(ctx, exprFun.apply(x), GeoJsonObject.class, false, "line");

            final ShapeFactory shpFactory = spatialContext.getShapeFactory();
            final List<org.locationtech.spatial4j.shape.Point> allPoints = new ArrayList<>();

            if (obj.type == GeoJsonType.LineString) {
                final List<Coordinates> allCoordinates = ((LineString) obj).coordinates;
                if (allCoordinates == null) {
                    throw new InvalidResultTypeException("Null coordinates given to geo.length");
                }
                for (Coordinates coordinates : allCoordinates) {
                    allPoints.add(shpFactory.pointLatLon(coordinates.latitude, coordinates.longitude));
                }
            } else {
                throw new InvalidResultTypeException("Unsupported input for geo.length", "geography linestring", obj);
            }

            final DistanceCalculator distCalc = spatialContext.getDistCalc();
            double length = 0;
            org.locationtech.spatial4j.shape.Point previousPoint = null;
            for (org.locationtech.spatial4j.shape.Point nextPoint : allPoints) {
                if (previousPoint == null) {
                    previousPoint = nextPoint;
                    continue;
                }

                double arcLength = distCalc.distance(previousPoint, nextPoint);
                double mLength = DistanceUtils.degrees2Dist(arcLength, DistanceUtils.EARTH_MEAN_RADIUS_KM * 1000);
                length += mLength;
                previousPoint = nextPoint;
            }
            return length;

        };
    }

    private org.locationtech.spatial4j.shape.Point spatialPoint(final ShapeFactory shpFactory, final Point geoPoint) {
        return spatialPoint(shpFactory, geoPoint.coordinates);
    }

    private org.locationtech.spatial4j.shape.Point spatialPoint(final ShapeFactory shpFactory,
            final Coordinates geoCoords) {
        return shpFactory.pointLatLon(geoCoords.latitude, geoCoords.longitude);
    }

    private Function<ResourceValueFilterInputHolder, Object> runGeoDistance(ParserRuleContext ctx) {
        final SpatialContext spatialContext = SpatialContext.GEO;
        final ShapeFactory shpFactory = spatialContext.getShapeFactory();

        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);

        return x -> {
            Point left = convert(ctx, leftFun.apply(x), Point.class, false, "left");
            Point right = convert(ctx, rightFun.apply(x), Point.class, false, "right");
            return spatialContext.calcDistance(spatialPoint(shpFactory, left), spatialPoint(shpFactory, right));
        };
    }
}

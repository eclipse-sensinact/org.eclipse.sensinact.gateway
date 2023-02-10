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
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.gateway.geojson.GeometryCollection;
import org.eclipse.sensinact.gateway.geojson.LineString;
import org.eclipse.sensinact.gateway.geojson.MultiLineString;
import org.eclipse.sensinact.gateway.geojson.MultiPoint;
import org.eclipse.sensinact.gateway.geojson.MultiPolygon;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolmethodcallexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.CommonexprContext;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.locationtech.spatial4j.shape.ShapeFactory.LineStringBuilder;
import org.locationtech.spatial4j.shape.ShapeFactory.MultiLineStringBuilder;
import org.locationtech.spatial4j.shape.ShapeFactory.MultiPointBuilder;
import org.locationtech.spatial4j.shape.ShapeFactory.MultiPolygonBuilder;
import org.locationtech.spatial4j.shape.ShapeFactory.MultiShapeBuilder;
import org.locationtech.spatial4j.shape.ShapeFactory.PolygonBuilder;
import org.locationtech.spatial4j.shape.ShapeFactory.PolygonBuilder.HoleBuilder;
import org.locationtech.spatial4j.shape.SpatialRelation;

public class BooleanMethodCallExprVisitor
        extends ODataFilterBaseVisitor<Function<ResourceValueFilterInputHolder, Boolean>> {

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
            // Shape intersection
            return runGeoRelation(child, (l, r) -> l.relate(r) != SpatialRelation.DISJOINT);

        case ODataFilterParser.RULE_stdisjointmethodcallexpr:
            return runGeoRelation(child, (l, r) -> l.relate(r) == SpatialRelation.DISJOINT);

        case ODataFilterParser.RULE_stcontainssmethodcallexpr:
            return runGeoRelation(child, (l, r) -> l.relate(r) == SpatialRelation.CONTAINS);

        case ODataFilterParser.RULE_stwithinmethodcallexpr:
            return runGeoRelation(child, (l, r) -> r.relate(l) == SpatialRelation.CONTAINS);

        case ODataFilterParser.RULE_stequalsmethodcallexpr:
            return runGeoRelation(child, (l, r) -> l.equals(r));

        case ODataFilterParser.RULE_strelatesmethodcallexpr:
            return runSpatialRelates(child);

        case ODataFilterParser.RULE_stoverlapsmethodcallexpr:
        case ODataFilterParser.RULE_stcrossesmethodcallexpr:
        case ODataFilterParser.RULE_sttouchesmethodcallexpr:
            throw new UnsupportedRuleException("Unsupported geometry method", parser, child);

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

    private Shape spatialShape(final ShapeFactory factory, final GeoJsonObject object) {

        switch (object.type) {
        case Feature:
            return spatialShape(factory, ((Feature) object).geometry);

        case FeatureCollection: {
            final FeatureCollection collection = (FeatureCollection) object;
            final MultiShapeBuilder<Shape> builder = factory.multiShape(Shape.class);
            collection.features.stream().forEachOrdered(f -> builder.add(spatialShape(factory, f)));
            return builder.build();
        }

        case GeometryCollection: {
            final GeometryCollection collection = (GeometryCollection) object;
            final MultiShapeBuilder<Shape> builder = factory.multiShape(Shape.class);
            collection.geometries.stream().forEachOrdered(g -> builder.add(spatialShape(factory, g)));
            return builder.build();
        }

        default:
            return spatialShape(factory, (Geometry) object);
        }
    }

    private Shape spatialShape(final ShapeFactory factory, final Geometry geometry) {
        switch (geometry.type) {
        case Point: {
            final Coordinates coords = ((Point) geometry).coordinates;
            return factory.pointLatLon(coords.latitude, coords.longitude);
        }

        case LineString: {
            final LineString line = (LineString) geometry;
            final LineStringBuilder builder = factory.lineString();
            line.coordinates.stream().forEachOrdered(c -> builder.pointLatLon(c.latitude, c.longitude));
            return builder.build();
        }

        case Polygon: {
            final Polygon polygon = (Polygon) geometry;
            final PolygonBuilder builder = factory.polygon();
            polygon.coordinates.get(0).stream().forEachOrdered(c -> builder.pointLatLon(c.latitude, c.longitude));
            return builder.buildOrRect();
        }

        case MultiPoint: {
            final MultiPoint points = (MultiPoint) geometry;
            final MultiPointBuilder builder = factory.multiPoint();
            points.coordinates.stream().forEachOrdered(c -> builder.pointLatLon(c.latitude, c.longitude));
            return builder.build();
        }

        case MultiLineString: {
            final MultiLineString lines = (MultiLineString) geometry;
            final MultiLineStringBuilder builder = factory.multiLineString();
            for (List<Coordinates> line : lines.coordinates) {
                final LineStringBuilder subBuilder = factory.lineString();
                line.stream().forEachOrdered(c -> subBuilder.pointLatLon(c.latitude, c.longitude));
                builder.add(subBuilder);
            }
            return builder.build();
        }

        case MultiPolygon: {
            final MultiPolygon polygons = (MultiPolygon) geometry;
            final MultiPolygonBuilder builder = factory.multiPolygon();
            for (List<List<Coordinates>> subPolygon : polygons.coordinates) {
                final PolygonBuilder initBuilder = factory.polygon();
                subPolygon.get(0).stream().forEachOrdered(c -> initBuilder.pointLatLon(c.latitude, c.longitude));

                PolygonBuilder subBuilder = initBuilder;
                final List<List<Coordinates>> holes = new ArrayList<>(subPolygon);
                holes.remove(0);
                if (!holes.isEmpty()) {
                    for (List<Coordinates> hole : holes) {
                        final HoleBuilder holeBuilder = subBuilder.hole();
                        hole.stream().forEachOrdered(c -> holeBuilder.pointLatLon(c.latitude, c.longitude));
                        subBuilder = holeBuilder.endHole();
                    }
                }
                builder.add(subBuilder);
            }
            return builder.build();
        }

        default:
            throw new ParsingException("Unsupported Geometry: " + geometry.type);
        }
    }

    private Function<ResourceValueFilterInputHolder, Boolean> runGeoRelation(ParserRuleContext ctx,
            BiFunction<Shape, Shape, Boolean> predicate) {
        final SpatialContext spatialContext = SpatialContext.GEO;
        final ShapeFactory shpFactory = spatialContext.getShapeFactory();

        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);

        return x -> {
            GeoJsonObject left = convert(ctx, leftFun.apply(x), GeoJsonObject.class, false, "arg1");
            GeoJsonObject right = convert(ctx, rightFun.apply(x), GeoJsonObject.class, false, "arg2");

            // Convert GeoJSON back to shapes
            Shape leftShape = spatialShape(shpFactory, left);
            Shape rightShape = spatialShape(shpFactory, right);
            return predicate.apply(leftShape, rightShape);
        };
    }

    private Function<ResourceValueFilterInputHolder, Boolean> runSpatialRelates(ParserRuleContext ctx) {
        final SpatialContext spatialContext = SpatialContext.GEO;
        final ShapeFactory shpFactory = spatialContext.getShapeFactory();

        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);
        final CommonexprContext relationExpr = ctx.getChild(CommonexprContext.class, 2);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);
        final Function<ResourceValueFilterInputHolder, Object> relationFun = visitor.visitCommonexpr(relationExpr);

        return x -> {
            GeoJsonObject left = convert(ctx, leftFun.apply(x), GeoJsonObject.class, false, "arg1");
            GeoJsonObject right = convert(ctx, rightFun.apply(x), GeoJsonObject.class, false, "arg2");
            String strRelation = convert(ctx, relationFun.apply(x), String.class, false, "arg3");

            // Load relation
            SpatialRelation relation = SpatialRelation.valueOf(strRelation);

            // Convert GeoJSON back to shapes
            Shape leftShape = spatialShape(shpFactory, (GeoJsonObject) left);
            Shape rightShape = spatialShape(shpFactory, (GeoJsonObject) right);
            return leftShape.relate(rightShape) == relation;
        };
    }
}

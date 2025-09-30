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

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

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
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographycollectionContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographylinestringContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographymultilinestringContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographymultipointContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographymultipolygonContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographypointContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographypolygonContext;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;
import org.locationtech.spatial4j.shape.impl.BufferedLineString;

public class GeoGeographyVisitor extends ODataFilterBaseVisitor<GeoJsonObject> {

    private final SpatialContext spatialContext = SpatialContext.GEO;
    private final ShapeReader shapeReader;

    public GeoGeographyVisitor() {
        this.shapeReader = spatialContext.getFormats().getWktReader();
    }

    private Shape parseShape(ParserRuleContext ctx) {
        try {
            return shapeReader.read(ctx.getText());
        } catch (ParseException | InvalidShapeException | IOException e) {
            throw new ParsingException("Error parsing geographic point: " + ctx.getText(), e);
        }
    }

    private Coordinates makeCoordinates(org.locationtech.spatial4j.shape.Point shpPoint) {
        return makeCoordinates(shpPoint.getLon(), shpPoint.getLat());
    }

    private Coordinates makeCoordinates(double lon, double lat) {
        return new Coordinates(lon, lat);
    }

    private List<Double> makeBbox(Shape shape) {
        final Rectangle boundingBox = shape.getBoundingBox();
        return List.of(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMaxX(), boundingBox.getMaxY());
    }

    @Override
    public Point visitGeographypoint(GeographypointContext ctx) {
        final org.locationtech.spatial4j.shape.Point parsedPoint = (org.locationtech.spatial4j.shape.Point) parseShape(
                ctx.fullpointliteral());
        return new Point(makeCoordinates(parsedPoint), null, null);
    }

    @Override
    public LineString visitGeographylinestring(GeographylinestringContext ctx) {
        final BufferedLineString parsedLine = (BufferedLineString) parseShape(ctx.fulllinestringliteral());
        List<Coordinates> line = parsedLine.getPoints().stream().map(this::makeCoordinates).collect(toList());
        return new LineString(line, makeBbox(parsedLine), null);
    }

    private boolean checkAllShapes(ShapeCollection<? extends Shape> shapes, Class<? extends Shape> classToTest) {
        return shapes.getShapes().stream().allMatch(s -> classToTest.isAssignableFrom(s.getClass()));
    }

    @SuppressWarnings("unchecked")
    private Geometry makeGeometry(Shape shape) {
        if (shape instanceof org.locationtech.spatial4j.shape.Point) {
            return new Point(makeCoordinates((org.locationtech.spatial4j.shape.Point) shape), null, null);
        } else if (shape instanceof BufferedLineString) {
            final BufferedLineString parsedLine = (BufferedLineString) shape;
            List<Coordinates> line = parsedLine.getPoints().stream().map(this::makeCoordinates).collect(toList());
            return new LineString(line, makeBbox(parsedLine), null);
        } else if (shape instanceof Rectangle) {
            return makePolygon((Rectangle) shape);
        } else if (shape instanceof ShapeCollection) {
            final ShapeCollection<? extends Shape> shapes = ((ShapeCollection<? extends Shape>) shape);
            final Shape firstShape = shapes.getShapes().get(0);
            if (firstShape instanceof org.locationtech.spatial4j.shape.Point
                    && checkAllShapes(shapes, org.locationtech.spatial4j.shape.Point.class)) {
                // Multipoint
                return makeMultiPoint((ShapeCollection<org.locationtech.spatial4j.shape.Point>) shapes);
            } else if (firstShape instanceof BufferedLineString && checkAllShapes(shapes, BufferedLineString.class)) {
                // Multilinestring
                return makeMultiLineString((ShapeCollection<BufferedLineString>) shapes);
            } else if (firstShape instanceof Rectangle && checkAllShapes(shapes, Rectangle.class)) {
                // Multipolygon
                return makeMultiPolygon(shapes);
            } else {
                // Collection
                List<Geometry> geoms = shapes.getShapes().stream().map(this::makeGeometry).collect(toList());
                return new GeometryCollection(geoms, makeBbox(shapes.getBoundingBox()), null);
            }
        }

        throw new ParsingException("Unsupported shape: " + shape);
    }

    private Feature makeFeature(String id, Shape shape) {
        return new Feature(id, makeGeometry(shape), null, makeBbox(shape.getBoundingBox()), null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FeatureCollection visitGeographycollection(GeographycollectionContext ctx) {
        final int line = ctx.getStart().getLine();
        final int charPositionInLine = ctx.getStart().getCharPositionInLine();
        AtomicInteger i = new AtomicInteger();
        Supplier<String> idGenerator = () -> String.format("feature_collection_%d_%d_%d", line, charPositionInLine, i.incrementAndGet());

        final ShapeCollection<Shape> shpCollection = (ShapeCollection<Shape>) parseShape(ctx.fullcollectionliteral());

        List<Feature> features = shpCollection.getShapes().stream().map(s -> makeFeature(idGenerator.get(), s)).collect(toList());
        return new FeatureCollection(features, makeBbox(shpCollection), null);
    }

    private MultiLineString makeMultiLineString(ShapeCollection<BufferedLineString> shpCollection) {
        List<List<Coordinates>> lines = shpCollection.getShapes().stream()
                .map(lineString -> lineString.getPoints().stream().map(this::makeCoordinates).collect(toList()))
                .collect(toList());
        return new MultiLineString(lines, makeBbox(shpCollection.getBoundingBox()), null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultiLineString visitGeographymultilinestring(GeographymultilinestringContext ctx) {
        final ShapeCollection<BufferedLineString> shpCollection = (ShapeCollection<BufferedLineString>) parseShape(
                ctx.fullmultilinestringliteral());
        return makeMultiLineString(shpCollection);
    }

    private MultiPoint makeMultiPoint(ShapeCollection<org.locationtech.spatial4j.shape.Point> shpCollection) {
        List<Coordinates> points = shpCollection.getShapes().stream().map(this::makeCoordinates).collect(toList());
        return new MultiPoint(points, makeBbox(shpCollection), null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultiPoint visitGeographymultipoint(GeographymultipointContext ctx) {
        final ShapeCollection<org.locationtech.spatial4j.shape.Point> shpCollection = (ShapeCollection<org.locationtech.spatial4j.shape.Point>) parseShape(
                ctx.fullmultipointliteral());
        return makeMultiPoint(shpCollection);
    }

    private Polygon makePolygon(final Shape shape) {
        if (shape instanceof Rectangle) {
            final Rectangle rect = (Rectangle) shape;
            final List<Coordinates> rectPoints = new ArrayList<>();
            rectPoints.add(makeCoordinates(rect.getMinX(), rect.getMinY()));
            rectPoints.add(makeCoordinates(rect.getMinX(), rect.getMaxY()));
            rectPoints.add(makeCoordinates(rect.getMaxX(), rect.getMaxY()));
            rectPoints.add(makeCoordinates(rect.getMaxX(), rect.getMinY()));
            rectPoints.add(makeCoordinates(rect.getMinX(), rect.getMinY()));
            return new Polygon(List.of(rectPoints), makeBbox(shape), null);
        } else {
            throw new UnsupportedRuleException("Polygons are not supported");
        }
    }

    private MultiPolygon makeMultiPolygon(final ShapeCollection<? extends Shape> shpCollection) {
        List<List<List<Coordinates>>> polys = shpCollection.getShapes().stream().map(this::makePolygon).map(p -> p.coordinates())
                .collect(toList());
        return new MultiPolygon(polys, makeBbox(shpCollection), null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultiPolygon visitGeographymultipolygon(GeographymultipolygonContext ctx) {
        final ShapeCollection<Shape> shpCollection = (ShapeCollection<Shape>) parseShape(ctx.fullmultipolygonliteral());
        return makeMultiPolygon(shpCollection);
    }

    @Override
    public Polygon visitGeographypolygon(GeographypolygonContext ctx) {
        final Shape shape = parseShape(ctx.fullpolygonliteral());
        return makePolygon(shape);
    }
}

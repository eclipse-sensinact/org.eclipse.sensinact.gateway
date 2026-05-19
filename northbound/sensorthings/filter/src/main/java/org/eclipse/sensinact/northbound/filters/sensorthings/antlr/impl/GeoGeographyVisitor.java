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

import static com.esri.core.geometry.WktImportFlags.wktImportDefaults;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographycollectionContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographylinestringContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographymultilinestringContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographymultipointContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographymultipolygonContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographypointContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeographypolygonContext;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.GeometryException;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;

public class GeoGeographyVisitor extends ODataFilterBaseVisitor<Geometry> {

    /**
     * The coordinate system used in GeoJSON
     */
    private static final SpatialReference WGS84_COORDS = SpatialReference.create("4326");

    private Geometry parseShape(ParserRuleContext ctx, Geometry.Type type) {
        try {
            return GeometryEngine.geometryFromWkt(ctx.getText(), wktImportDefaults, type);
        } catch (GeometryException | IllegalArgumentException e) {
            throw new ParsingException("Error parsing geographic point: " + ctx.getText(), e);
        }
    }

    @Override
    public Point visitGeographypoint(GeographypointContext ctx) {
        return (Point) parseShape(ctx.fullpointliteral(), Type.Point);
    }

    @Override
    public Polyline visitGeographylinestring(GeographylinestringContext ctx) {
        return (Polyline) parseShape(ctx.fulllinestringliteral(), Type.Polyline);
    }

    @Override
    public Geometry visitGeographycollection(GeographycollectionContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    protected Geometry aggregateResult(Geometry aggregate, Geometry nextResult) {
        final Geometry a,b;
        if(aggregate.getDimension() == nextResult.getDimension()) {
            // If dimensions are the same we can union directly
            a = aggregate;
            b = nextResult;
        } else {
            // If dimensions are different then transform them
            a = mapTo2D(aggregate);
            b = mapTo2D(nextResult);
        }
        return GeometryEngine.union(new Geometry[] {a, b}, WGS84_COORDS);
    }

    private Geometry mapTo2D(Geometry g) {
        // Do a minimal buffer to make all the constituents all 2d. This buffer
        // is approximately 0.1mm at the equator, reaching 1m at +/- 89.9 degrees
        return g.getDimension() == 2 ? g : GeometryEngine.buffer(g, WGS84_COORDS, 0.000000001d);
    }

    @Override
    public Polyline visitGeographymultilinestring(GeographymultilinestringContext ctx) {
        return (Polyline) parseShape(ctx.fullmultilinestringliteral(), Type.Polyline);
    }

    @Override
    public MultiPoint visitGeographymultipoint(GeographymultipointContext ctx) {
        return (MultiPoint) parseShape(ctx.fullmultipointliteral(), Type.MultiPoint);
    }

    @Override
    public Polygon visitGeographymultipolygon(GeographymultipolygonContext ctx) {
        return (Polygon) parseShape(ctx.fullmultipolygonliteral(), Type.Polygon);
    }

    @Override
    public Polygon visitGeographypolygon(GeographypolygonContext ctx) {
        return (Polygon) parseShape(ctx.fullpolygonliteral(), Type.Polygon);
    }
}

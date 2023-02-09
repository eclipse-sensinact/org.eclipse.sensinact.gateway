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

import java.io.IOException;
import java.text.ParseException;

import org.antlr.v4.runtime.Parser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeoliteralContext;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.shape.Shape;

public class GeoGeographyVisitor extends ODataFilterBaseVisitor<Shape> {

    private final Parser parser;
    private final SpatialContext spatialContext = SpatialContext.GEO;
    private final ShapeReader shapeReader;

    public GeoGeographyVisitor(final Parser parser) {
        this.parser = parser;
        this.shapeReader = spatialContext.getFormats().getWktReader();
    }

    @Override
    public Shape visitGeoliteral(GeoliteralContext ctx) {
        try {
            return shapeReader.read(ctx.getText());
        } catch (ParseException | InvalidShapeException | IOException e) {
            throw new ParsingException("Error parsing geographic point", e);
        }
    }

//
//    @Override
//    public Shape visitGeographypoint(GeographypointContext ctx) {
//        try {
//            return shapeReader.read(ctx.fullpointliteral().getText());
//        } catch (ParseException | InvalidShapeException | IOException e) {
//            throw new ParsingException("Error parsing geographic point", e);
//        }
//    }
//
//    @Override
//    public Shape visitGeographylinestring(GeographylinestringContext ctx) {
//        try {
//            return shapeReader.read(ctx.fulllinestringliteral().getText());
//        } catch (ParseException | InvalidShapeException | IOException e) {
//            throw new ParsingException("Error parsing geographic point", e);
//        }
//    }
}

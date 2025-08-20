/*********************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.geojson.internal;

import java.io.IOException;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.utils.GeoJsonUtils;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/**
 * A Jackson deserializer for {@link Point} objects as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1.1">the GeoJSON
 * specification</a>
 */
@SuppressWarnings("serial")
public class PointDeserializer extends StdNodeBasedDeserializer<Point> {

    public PointDeserializer() {
        super(Point.class);
    }

    @Override
    public Point convert(JsonNode root, DeserializationContext ctxt) throws IOException {
        if (root.get("coordinates") != null) {
            JsonNode coordNode = root.get("coordinates");
            if (coordNode.isArray() && coordNode.isEmpty()) {
                return null;
            } else if (coordNode.isArray() && coordNode.size() >= 2) {
                Coordinates c = new Coordinates();
                c.longitude = coordNode.get(0).asDouble();
                c.latitude = coordNode.get(1).asDouble();
                    if (coordNode.size() >= 3) {
                        c.elevation = coordNode.get(2).asDouble();
                    } else {
                        c.elevation = Double.NaN;
                    }
                return GeoJsonUtils.point(c);
            } else {
                throw MismatchedInputException.from(ctxt.getParser(), Point.class,
                    "GeoJSON coordinates must always be a list of at least two elements");
            }
        } else {
            throw MismatchedInputException.from(ctxt.getParser(), Point.class,
                "GeoJSON point must always contain a coordinates node");
        }
    }
}

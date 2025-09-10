/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
*   Tim Ward - refactor as records
**********************************************************************/
package org.eclipse.sensinact.gateway.geojson.internal;

import java.io.IOException;

import org.eclipse.sensinact.gateway.geojson.Coordinates;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/**
 * A Jackson deserializer for {@link Coordinates} objects as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1.1">the GeoJSON
 * specification</a>
 */
@SuppressWarnings("serial")
public class CoordinatesDeserializer extends StdNodeBasedDeserializer<Coordinates> {

    public CoordinatesDeserializer() {
        super(Coordinates.class);
    }

    @SuppressWarnings("resource")
    @Override
    public Coordinates convert(JsonNode root, DeserializationContext ctxt) throws IOException {
        if (root.isArray()) {
            if(root.isEmpty()) {
                // GeoJSON specification 3.1 - GeoJSON processors MAY interpret Geometry objects with
                // empty "coordinates" arrays as null objects.
                return Coordinates.EMPTY;
            } else if(root.size() >= 2) {
                double longitude = root.get(0).asDouble();
                double latitude = root.get(1).asDouble();
                if(!Double.isFinite(longitude) || !Double.isFinite(latitude)) {
                    throw MismatchedInputException.from(ctxt.getParser(), Coordinates.class,
                            "GeoJSON coordinates must have finite latitude and longitude");
                }
                double elevation;
                if (root.size() >= 3) {
                    elevation = root.get(2).asDouble();
                } else {
                    // We use NaN as a marker indicating "no elevation"
                    elevation = Double.NaN;
                }
                return new Coordinates(longitude, latitude, elevation);
            }
        }
        throw MismatchedInputException.from(ctxt.getParser(), Coordinates.class,
                "GeoJSON coordinates must always be a list of at least two elements");
    }
}

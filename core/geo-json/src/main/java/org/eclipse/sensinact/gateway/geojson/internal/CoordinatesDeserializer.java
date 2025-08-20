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

    @Override
    public Coordinates convert(JsonNode root, DeserializationContext ctxt) throws IOException {
        if (root.isArray() && root.size() >= 2) {
            Coordinates c = new Coordinates();
            c.longitude = root.get(0).asDouble();
            c.latitude = root.get(1).asDouble();
            if (root.size() >= 3) {
                c.elevation = root.get(2).asDouble();
            } else {
                c.elevation = Double.NaN;
            }
            return c;
        } else if (root.isArray() && root.isEmpty()) {
            return null;
        } else {
            throw MismatchedInputException.from(ctxt.getParser(), Coordinates.class,
                    "GeoJSON coordinates must always be a list of at least two elements");
        }
    }
}

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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A Jackson serializer for {@link Coordinates} objects as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1.1">the GeoJSON
 * specification</a>
 */
@SuppressWarnings("serial")
public class CoordinatesSerializer extends StdSerializer<Coordinates> {

    private static final double[] EMPTY = new double[0];

    public CoordinatesSerializer() {
        super(Coordinates.class);
    }

    @Override
    public void serialize(Coordinates value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        double[] array = value.isEmpty() ? EMPTY : value.hasElevation() ?
                new double[] { value.longitude(), value.latitude(), value.elevation() } : new double[] { value.longitude(), value.latitude() };
        gen.writeArray(array, 0, array.length);
    }
}

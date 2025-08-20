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
package org.eclipse.sensinact.gateway.geojson;

import java.util.Objects;

import org.eclipse.sensinact.gateway.geojson.internal.CoordinatesSerializer;
import org.eclipse.sensinact.gateway.geojson.internal.PointDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A GeoJSON point object as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1">the GeoJSON
 * specification</a>
 */
@JsonDeserialize(using = PointDeserializer.class)
<<<<<<< Upstream, based on branch 'geojson-empty-point' of git@github.com:isalvadori/org.eclipse.sensinact.gateway.git
<<<<<<< Upstream, based on branch 'geojson-empty-point' of git@github.com:isalvadori/org.eclipse.sensinact.gateway.git
=======
//@JsonSerialize(using = CoordinatesSerializer.class)
>>>>>>> d20e5f7 First version of PointDeserializer
=======
@JsonSerialize(using = CoordinatesSerializer.class)
>>>>>>> f9614eb re-enabled serializer for Point
public class Point extends Geometry {

    public Point() {
        super(GeoJsonType.Point);
    }

    public Coordinates coordinates;

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), coordinates);
    }

    @Override
    public boolean equals(Object obj) {
        if (checkParentEquals(obj)) {
            return Objects.equals(coordinates, ((Point) obj).coordinates);
        }
        return false;
    }

    @Override
    protected boolean getObjectDescription(StringBuilder builder) {
        builder.append("coords=").append(coordinates);
        return true;
    }
}

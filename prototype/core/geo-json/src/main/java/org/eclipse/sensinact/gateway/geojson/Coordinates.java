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

import org.eclipse.sensinact.gateway.geojson.internal.CoordinatesDeserializer;
import org.eclipse.sensinact.gateway.geojson.internal.CoordinatesSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A GeoJSON coordinate. We do not support additional point data beyond the
 * three entries defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1.1">the GeoJSON
 * specification</a>
 */
@JsonDeserialize(using = CoordinatesDeserializer.class)
@JsonSerialize(using = CoordinatesSerializer.class)
public class Coordinates {

    public double longitude;

    public double latitude;

    /**
     * The elevation will be {@link Double#NaN} if not set
     */
    public double elevation = Double.NaN;

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, elevation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == Coordinates.class) {
            // Chosen according to https://xkcd.com/2170/
            final double epsilon = 0.0000001d;
            final Coordinates other = (Coordinates) obj;
            if (Math.abs(other.longitude - longitude) >= epsilon) {
                return false;
            }

            if (Math.abs(other.latitude - latitude) >= epsilon) {
                return false;
            }

            if (Double.isFinite(other.elevation) && Double.isFinite(elevation)) {
                // Check elevations if both are finite
                return Math.abs(other.latitude - latitude) < epsilon;
            }

            // Equality if both elevations are not finite
            return !Double.isFinite(other.elevation) && !Double.isFinite(elevation);
        }

        return false;
    }

    @Override
    public String toString() {
        String repr = "(lon=" + longitude + ",lat=" + latitude;
        if (Double.isFinite(elevation)) {
            // Add elevation
            repr += ",alt=" + elevation;
        }
        return repr + ")";
    }
}

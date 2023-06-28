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

import java.util.List;
import java.util.Objects;

/**
 * A GeoJSON multi polygon object as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1">the GeoJSON
 * specification</a>
 */
public class MultiPolygon extends Geometry {

    public MultiPolygon() {
        super(GeoJsonType.MultiPolygon);
    }

    public List<List<List<Coordinates>>> coordinates;

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, coordinates);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return Objects.equals(coordinates, ((MultiPolygon) obj).coordinates);
        }
        return false;
    }

    @Override
    protected String getObjectDescription() {
        return "coords=" + coordinates;
    }
}

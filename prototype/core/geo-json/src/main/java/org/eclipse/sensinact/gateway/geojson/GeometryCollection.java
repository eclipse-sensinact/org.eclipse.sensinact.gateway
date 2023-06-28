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
 * A GeoJSON geometry collection object as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1">the GeoJSON
 * specification</a>
 */
public class GeometryCollection extends Geometry {

    public GeometryCollection() {
        super(GeoJsonType.GeometryCollection);
    }

    public List<Geometry> geometries;

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), geometries);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GeometryCollection) {
            return Objects.equals(geometries, ((GeometryCollection) obj).geometries);
        }
        return false;
    }

    @Override
    protected String getObjectDescription() {
        return "geometries=" + geometries;
    }
}

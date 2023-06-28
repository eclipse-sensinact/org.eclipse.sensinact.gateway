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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Feature extends GeoJsonObject {

    public Map<String, Object> properties = new HashMap<>();

    public Geometry geometry;

    public String id;

    public Feature() {
        super(GeoJsonType.Feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, geometry, properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final Feature other = (Feature) obj;
            return Objects.equals(id, other.id) && Objects.equals(geometry, ((Feature) obj).geometry)
                    && Objects.equals(properties, other.properties);
        }
        return false;
    }

    @Override
    protected String getObjectDescription() {
        return String.format("id=%s, geometry=%s, properties=%s", id, geometry, properties);
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FeatureCollection extends GeoJsonObject {

    public List<Feature> features = new ArrayList<>();

    public FeatureCollection() {
        super(GeoJsonType.FeatureCollection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), features);
    }

    @Override
    public boolean equals(Object obj) {
        if (checkParentEquals(obj)) {
            return Objects.equals(features, ((FeatureCollection) obj).features);
        }
        return false;
    }

    @Override
    protected boolean getObjectDescription(StringBuilder builder) {
        builder.append("features=").append(features);
        return true;
    }
}

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
}

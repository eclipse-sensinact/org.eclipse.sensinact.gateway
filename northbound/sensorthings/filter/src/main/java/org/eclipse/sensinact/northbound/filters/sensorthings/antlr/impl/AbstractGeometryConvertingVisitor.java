/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.gateway.filters.esri.geometry.EsriUtils;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;

import com.esri.core.geometry.Geometry;

public abstract class AbstractGeometryConvertingVisitor<T> extends ODataFilterBaseVisitor<T> {

    @SuppressWarnings("unchecked")
    protected <R> R convert(final ParserRuleContext ctx, final Object object, final Class<R> type,
            final boolean allowNull, final String placeDescription) {
        if (object == null) {
            if (allowNull) {
                return null;
            } else {
                throw new InvalidResultTypeException(placeDescription + " is null", type.getSimpleName(), object);
            }
        }

        if(Geometry.class.isAssignableFrom(type) && GeoJsonObject.class.isInstance(object)) {
            return (R) EsriUtils.toEsriGeometry((GeoJsonObject) object);
        }

        if (!type.isAssignableFrom(object.getClass())) {
            throw new InvalidResultTypeException("Unsupported " + placeDescription + " for \"" + ctx.getText() + "\"",
                    type.getSimpleName(), object);
        }

        return (R) object;
    }
}

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
*   Kentyou - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.emf.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.GeoJsonType;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.model.nexus.impl.emf.EMFUtil;
import org.junit.jupiter.api.Test;

public class EMFUtilTest {

    @Test
    void testConvertStringToGeoJson() {

        String point = "{\"type\": \"Point\", \"coordinates\": [12.3,45.6]}";
        Point o = (Point) EMFUtil.convertToTargetType(GeoJsonObject.class, point);

        assertEquals(GeoJsonType.Point, o.type);
        assertEquals(12.3d, o.coordinates.longitude);
        assertEquals(45.6d, o.coordinates.latitude);

        o = (Point) EMFUtil.convertToTargetType(SensiNactPackage.eINSTANCE.getEGeoJsonObject(), point);

        assertEquals(GeoJsonType.Point, o.type);
        assertEquals(12.3d, o.coordinates.longitude);
        assertEquals(45.6d, o.coordinates.latitude);
    }

    @Test
    void testConvertMapToGeoJson() {

        Map<String, Object> point = Map.of("type", "Point", "coordinates", new double[] { 12.3, 45.6 });
        Point o = (Point) EMFUtil.convertToTargetType(GeoJsonObject.class, point);

        assertEquals(GeoJsonType.Point, o.type);
        assertEquals(12.3d, o.coordinates.longitude);
        assertEquals(45.6d, o.coordinates.latitude);

        o = (Point) EMFUtil.convertToTargetType(SensiNactPackage.eINSTANCE.getEGeoJsonObject(), point);

        assertEquals(GeoJsonType.Point, o.type);
        assertEquals(12.3d, o.coordinates.longitude);
        assertEquals(45.6d, o.coordinates.latitude);
    }

    @Test
    void testConvertStringToNumber() {

        String num = "12";
        Number o = (Number) EMFUtil.convertToTargetType(Integer.class, num);

        assertEquals(12, o);

        o = (Number) EMFUtil.convertToTargetType(EcorePackage.eINSTANCE.getEIntegerObject(), num);

        assertEquals(12, o);
    }

}

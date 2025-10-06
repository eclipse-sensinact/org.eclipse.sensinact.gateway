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
package org.eclipse.sensinact.core.emf.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.GeoJsonType;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.testdata.TestdataPackage;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class EMFUtilTest {

    @Test
    void testConvertStringToGeoJson() {

        String point = "{\"type\": \"Point\", \"coordinates\": [12.3,45.6]}";
        Point o = (Point) EMFUtil.convertToTargetType(GeoJsonObject.class, point);

        assertEquals(GeoJsonType.Point, o.type());
        assertEquals(12.3d, o.coordinates().longitude());
        assertEquals(45.6d, o.coordinates().latitude());

        o = (Point) EMFUtil.convertToTargetType(ProviderPackage.eINSTANCE.getEGeoJsonObject(), point);

        assertEquals(GeoJsonType.Point, o.type());
        assertEquals(12.3d, o.coordinates().longitude());
        assertEquals(45.6d, o.coordinates().latitude());
    }

    @Test
    void testConvertMapToGeoJson() {

        Map<String, Object> point = Map.of("type", "Point", "coordinates", new double[] { 12.3, 45.6 });
        Point o = (Point) EMFUtil.convertToTargetType(GeoJsonObject.class, point);

        assertEquals(GeoJsonType.Point, o.type());
        assertEquals(12.3d, o.coordinates().longitude());
        assertEquals(45.6d, o.coordinates().latitude());

        o = (Point) EMFUtil.convertToTargetType(ProviderPackage.eINSTANCE.getEGeoJsonObject(), point);

        assertEquals(GeoJsonType.Point, o.type());
        assertEquals(12.3d, o.coordinates().longitude());
        assertEquals(45.6d, o.coordinates().latitude());
    }

    @Test
    void testConvertStringToNumber() {

        String num = "12";
        Number o = (Number) EMFUtil.convertToTargetType(Integer.class, num);

        assertEquals(12, o);

        o = (Number) EMFUtil.convertToTargetType(EcorePackage.eINSTANCE.getEIntegerObject(), num);

        assertEquals(12, o);
    }

    @Test
    void testGetModelName() {
        String modelName = EMFUtil.getModelName(TestdataPackage.Literals.TEST_SENSOR);
        assertEquals("TestSensor", modelName);
    }

    @Test
    void testGetModelNameAnnotation() {
        String modelName = EMFUtil.getModelName(TestdataPackage.Literals.TEST_MODEL_WITH_ANNOTATION);
        assertEquals("TestModel", modelName);
    }

    /**
     * Test record class. Must be public to be accessible by EMFUtil
     */
    public record Record(String name, int value) {
    }

    public class RecordDTO {
        public String name;
        public int value;
    }

    @Test
    void testRecordConversion() throws Exception {
        // Prepare input
        final ObjectMapper mapper = JsonMapper.builder().build();
        final Record record = new Record("test", 42);
        final RecordDTO dtoRecord = new RecordDTO();
        dtoRecord.name = record.name();
        dtoRecord.value = record.value();
        final Map<String, Object> mapRecord = mapper.convertValue(record, new TypeReference<Map<String, Object>>() {
        });

        // Test pass-through
        Record parsed = (Record) EMFUtil.convertToTargetType(Record.class, record);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);

        // Test from DTO
        parsed = (Record) EMFUtil.convertToTargetType(Record.class, dtoRecord);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);

        // Test from Map
        parsed = (Record) EMFUtil.convertToTargetType(Record.class, mapRecord);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);

        // Test to map
        @SuppressWarnings("unchecked")
        Map<String, Object> parsedMap = (Map<String, Object>) EMFUtil.convertToTargetType(Map.class, record);
        assertEquals(2, parsedMap.size());
        assertEquals("test", parsedMap.get("name"));
        assertEquals(42, parsedMap.get("value"));
    }
}

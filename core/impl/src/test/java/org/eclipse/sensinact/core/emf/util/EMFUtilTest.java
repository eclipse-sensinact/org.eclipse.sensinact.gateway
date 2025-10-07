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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public record SubRecord(String subName, Number subValue) {
    }

    public static class SubRecordDTO {
        public String subName;
        public Number subValue;

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof SubRecordDTO that) {
                return Objects.equals(this.subName, that.subName)
                    && Objects.equals(this.subValue, that.subValue);
            }
            return false;
        }
    }

    /**
     * Test record class. Must be public to be accessible by EMFUtil
     */
    public record Record(String name, int value, List<SubRecord> subRecords) {
    }

    public static class RecordDTO {
        public String name;
        public int value;
        public List<SubRecordDTO> subRecords;

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof RecordDTO that) {
                return Objects.equals(this.name, that.name)
                    && this.value == that.value
                    && Objects.equals(this.subRecords, that.subRecords);
            }
            return false;
        }
    }

    @Test
    void testRecordConversion() throws Exception {
        // Prepare input
        final ObjectMapper mapper = JsonMapper.builder().build();
        final Record record = new Record("test", 42, List.of(new SubRecord("a", 1), new SubRecord("b", 2.5)));
        final RecordDTO dtoRecord = new RecordDTO();
        dtoRecord.name = record.name();
        dtoRecord.value = record.value();
        dtoRecord.subRecords = record.subRecords().stream().map(sr -> {
            SubRecordDTO dto = new SubRecordDTO();
            dto.subName = sr.subName();
            dto.subValue = sr.subValue();
            return dto;
        }).toList();
        final Map<String, Object> mapRecord = mapper.convertValue(record, new TypeReference<Map<String, Object>>() {
        });

        // Test pass-through
        Record parsed = (Record) EMFUtil.convertToTargetType(Record.class, record);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);
        assertEquals(2, parsed.subRecords.size());
        assertInstanceOf(SubRecord.class, parsed.subRecords.get(0));
        assertEquals(record.subRecords.get(0), parsed.subRecords.get(0));
        assertEquals(record.subRecords.get(1), parsed.subRecords.get(1));

        // Test from DTO
        parsed = (Record) EMFUtil.convertToTargetType(Record.class, dtoRecord);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);
        assertInstanceOf(SubRecord.class, parsed.subRecords.get(0));
        assertEquals(record.subRecords.get(0), parsed.subRecords.get(0));
        assertEquals(record.subRecords.get(1), parsed.subRecords.get(1));

        // Test from Map
        parsed = (Record) EMFUtil.convertToTargetType(Record.class, mapRecord);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);
        assertEquals(record.subRecords.get(0), parsed.subRecords.get(0));
        assertEquals(record.subRecords.get(1), parsed.subRecords.get(1));
    }

    @Test
    void testDtoConversion() throws Exception {
        // Prepare input
        final ObjectMapper mapper = JsonMapper.builder().build();
        final RecordDTO dtoRecord = new RecordDTO();
        dtoRecord.name = "test";
        dtoRecord.value = 42;
        dtoRecord.subRecords = new ArrayList<>();
        SubRecordDTO subRecord = new SubRecordDTO();
        subRecord.subName = "a";
        subRecord.subValue = 1;
        dtoRecord.subRecords.add(subRecord);
        subRecord = new SubRecordDTO();
        subRecord.subName = "b";
        subRecord.subValue = 2.5;
        dtoRecord.subRecords.add(subRecord);
        final Map<String, Object> mapRecord = mapper.convertValue(dtoRecord, new TypeReference<Map<String, Object>>() {
        });

        // Test pass-through
        RecordDTO parsed = (RecordDTO) EMFUtil.convertToTargetType(RecordDTO.class, dtoRecord);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);
        assertEquals(2, parsed.subRecords.size());
        assertInstanceOf(SubRecordDTO.class, parsed.subRecords.get(0));
        assertEquals(dtoRecord.subRecords.get(0), parsed.subRecords.get(0));
        assertEquals(dtoRecord.subRecords.get(1), parsed.subRecords.get(1));

        // Test from Map
        parsed = (RecordDTO) EMFUtil.convertToTargetType(RecordDTO.class, mapRecord);
        assertEquals("test", parsed.name);
        assertEquals(42, parsed.value);
        assertInstanceOf(SubRecordDTO.class, parsed.subRecords.get(0));
        assertEquals(dtoRecord.subRecords.get(0), parsed.subRecords.get(0));
        assertEquals(dtoRecord.subRecords.get(1), parsed.subRecords.get(1));
    }
}

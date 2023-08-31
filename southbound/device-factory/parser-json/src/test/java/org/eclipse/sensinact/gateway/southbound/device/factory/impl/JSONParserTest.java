package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.parser.json.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.util.promise.Promises;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests JSON-based mapping
 */
public class JSONParserTest {
    private FactoryParserHandler deviceMapper;
    private ComponentServiceObjects<IDeviceMappingParser> cso;

    private List<BulkGenericDto> bulks = new ArrayList<>();

    @BeforeEach
    void start() throws InterruptedException {
        deviceMapper = new FactoryParserHandler();
        deviceMapper.dataUpdate = Mockito.mock(DataUpdate.class);

        Mockito.when(deviceMapper.dataUpdate.pushUpdate(Mockito.any())).thenAnswer(i -> {
            final BulkGenericDto dto = i.getArgument(0, BulkGenericDto.class);
            bulks.add(dto);
            return Promises.resolved(dto);
        });

        final JsonParser parser = new JsonParser();
        final DeviceMappingParserReference svcRef = new DeviceMappingParserReference();
        cso = new ComponentServiceObjects<IDeviceMappingParser>() {
            @Override
            public IDeviceMappingParser getService() {
                return parser;
            }

            @Override
            public ServiceReference<IDeviceMappingParser> getServiceReference() {
                return svcRef;
            }

            @Override
            public void ungetService(IDeviceMappingParser service) {
            }
        };
        deviceMapper.addParser(cso, Map.of(IDeviceMappingParser.PARSER_ID, "json"));
    }

    @AfterEach
    void stop() {
        deviceMapper.removeParser(cso, Map.of(IDeviceMappingParser.PARSER_ID, "json"));
        deviceMapper = null;
        cso = null;
        bulks.clear();
    }

    /**
     * Opens the given file from resources
     */
    byte[] readFile(final String filename) throws IOException {
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            return inStream.readAllBytes();
        }
    }

    /**
     * Reads the given mapping configuration
     */
    DeviceMappingConfigurationDTO readConfiguration(final String filename) throws IOException {
        return new ObjectMapper().readValue(readFile(filename), DeviceMappingConfigurationDTO.class);
    }

    /**
     * Get the DTO matching the given resource
     */
    GenericDto getResourceValue(final String provider, final String service, final String resource) {
        for (BulkGenericDto bulk : bulks) {
            for (GenericDto dto : bulk.dtos) {
                if (dto.provider.equals(provider) && dto.service.equals(service) && dto.resource.equals(resource)) {
                    return dto;
                }
            }
        }
        return null;
    }

    /**
     * Get the value of the resource in the bulks
     */
    <T> T getResourceValue(final String provider, final String service, final String resource, Class<T> rcType) {
        GenericDto dto = getResourceValue(provider, service, resource);
        if (dto != null && dto.value != null) {
            return rcType.cast(dto.value);
        }

        return null;
    }

    /**
     * Mapping a JSON file with an constants and default values
     */
    @Test
    void testLiteralMultipleObjects() throws Exception {
        // Excepted providers
        final String provider1 = "JsonLiteral1";
        final String provider2 = "JsonLiteral2";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("json/literal-mapping.json");

        // Read the file
        byte[] fileContent = readFile("json/literal.json");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // Ensure value and type
        assertEquals(94, getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(28, getResourceValue(provider2, "data", "value", Integer.class));

        // Ensure constant value
        assertEquals("Grenoble", getResourceValue(provider1, "sensor", "city", String.class));
        assertEquals("Grenoble", getResourceValue(provider2, "sensor", "city", String.class));

        // Ensure default value
        assertEquals("Cours Berriat", getResourceValue(provider1, "sensor", "street", String.class));
        assertEquals("n/a", getResourceValue(provider2, "sensor", "street", String.class));
    }

    /**
     * Mapping a JSON file with an array of objects
     */
    @Test
    void testMultipleObjects() throws Exception {
        // Excepted providers
        final String provider1 = "JsonMultiple1";
        final String provider2 = "JsonMultiple2";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("json/multiple-mapping.json");

        // Read the file
        byte[] fileContent = readFile("json/multiple.json");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // Ensure value and type
        assertEquals(94, getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(28, getResourceValue(provider2, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, getResourceValue(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, getResourceValue(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        GenericDto location1 = getResourceValue(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = (Point) location1.value;
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        GenericDto location2 = getResourceValue(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertEquals(1.5, geoPoint.coordinates.elevation, 0.001);
    }

    /**
     * Mapping a JSON file with an array of objects
     */
    @Test
    void testSubArray() throws Exception {
        // Excepted providers
        final String provider1 = "JsonSubArray1";
        final String provider2 = "JsonSubArray2";
        final String ignoredProvider = "JsonSubArray-Ignore";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("json/sub-array-mapping.json");

        // Read the file
        byte[] fileContent = readFile("json/sub-array.json");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // Ensure value and type
        assertEquals(94, getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(28, getResourceValue(provider2, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, getResourceValue(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, getResourceValue(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        GenericDto location1 = getResourceValue(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = (Point) location1.value;
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        GenericDto location2 = getResourceValue(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        // Ignored provider shoudln't be there
        for (BulkGenericDto bulk : bulks) {
            for (GenericDto dto : bulk.dtos) {
                assertNotEquals(ignoredProvider, dto.provider);
            }
        }
    }

    /**
     * Mapping a JSON file with a single object
     */
    @Test
    void testSingleObject() throws Exception {
        // Excepted provider
        final String provider = "JsonSingle1";

        // Read the configuration (reuse the one for multiple objects)
        DeviceMappingConfigurationDTO config = readConfiguration("json/multiple-mapping.json");

        // Read the file
        byte[] fileContent = readFile("json/single.json");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // Ensure value and type
        assertEquals(15, getResourceValue(provider, "data", "value", Integer.class));
        assertEquals(1691587953000L, getResourceValue(provider, "data", "long-value", Long.class));

        // Ensure timestamp
        Instant timestamp = Instant.from(LocalDateTime.of(2022, 12, 7, 15, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, getResourceValue(provider, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        GenericDto location = getResourceValue(provider, "admin", "location");
        assertEquals(timestamp, location.timestamp);
        assertNotNull(location.value);
        Point geoPoint = (Point) location.value;
        assertEquals(45.199, geoPoint.coordinates.latitude, 0.001);
        assertEquals(5.725, geoPoint.coordinates.longitude, 0.001);
        assertEquals(476, geoPoint.coordinates.elevation);
    }

    @Test
    void testDatetime() throws Exception {
        // Excepted provider
        final String provider = "JsonDatetime";

        // Read the configuration (reuse the one for multiple objects)
        DeviceMappingConfigurationDTO config = readConfiguration("json/datetime-mapping.json");

        // Read the file
        byte[] fileContent = readFile("json/datetime.json");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // Ensure timestamp
        Instant timestamp = Instant.from(LocalDateTime.of(2022, 12, 7, 12, 1, 58).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, getResourceValue(provider, "data", "value").timestamp);
    }

    /**
     * Tests nested objects
     */
    @Test
    void testDeepObjects() throws Exception {

        // Expected providers
        final String provider1 = "1452";
        final String type1 = "GOOSE";
        final String provider2 = "1851";
        final String type2 = "DUCK";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("json/deep-multiple-mapping.json");

        // Read the file
        byte[] fileContent = readFile("json/deep-multiple.json");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // Check first provider
        assertEquals(0, getResourceValue(provider1, "data", type1 + "-value", Integer.class));

        // Ensure timestamp
        Instant timestamp = Instant.from(LocalDateTime.of(2021, 10, 26, 15, 28, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, getResourceValue(provider1, "data", type1 + "-value").timestamp);

        // Ensure location update (and its timestamp)
        GenericDto location = getResourceValue(provider1, "admin", "location");
        assertEquals(timestamp, location.timestamp);
        assertNotNull(location.value);
        System.out.println("Location value: " + location.value + " - " + location.value.getClass());
        Point geoPoint = (Point) location.value;
        assertEquals(48.849577, geoPoint.coordinates.latitude, 0.001);
        assertEquals(2.350867, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        // Check 2nd provider
        assertEquals(7, getResourceValue(provider2, "data", type2 + "-value", Integer.class));

        // Ensure timestamp
        timestamp = Instant.from(LocalDateTime.of(2021, 10, 26, 15, 27, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, getResourceValue(provider2, "data", type2 + "-value").timestamp);

        // Ensure location update (and its timestamp)
        location = getResourceValue(provider2, "admin", "location");
        assertEquals(timestamp, location.timestamp);
        assertNotNull(location.value);
        geoPoint = (Point) location.value;
        assertEquals(48.858396, geoPoint.coordinates.latitude, 0.001);
        assertEquals(2.350484, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));
    }
}

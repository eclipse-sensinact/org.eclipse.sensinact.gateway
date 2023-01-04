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
package org.eclipse.sensinact.gateway.southbound.device.parser.integration.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingHandler;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests CSV-based mapping
 */
public class JSONParserTest {

    private static final String USER = "user";

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    @InjectService
    IDeviceMappingHandler deviceMapper;

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() {
        session = null;
    }

    /**
     * Opens the given file from resources
     */
    byte[] readFile(final String filename) throws IOException {
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream("/" + filename)) {
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
        byte[] csvContent = readFile("json/multiple.json");

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // Ensure value and type
        assertEquals(94, session.getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(28, session.getResourceValue(provider2, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, session.describeResource(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, session.describeResource(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        final ObjectMapper mapper = new ObjectMapper();
        ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = mapper.readValue(String.valueOf(location1.value), Point.class);
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        ResourceDescription location2 = session.describeResource(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = mapper.readValue(String.valueOf(location2.value), Point.class);
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
        byte[] csvContent = readFile("json/sub-array.json");

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));
        assertNull(session.describeProvider(ignoredProvider));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // Ensure value and type
        assertEquals(94, session.getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(28, session.getResourceValue(provider2, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, session.describeResource(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, session.describeResource(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        final ObjectMapper mapper = new ObjectMapper();
        ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = mapper.readValue(String.valueOf(location1.value), Point.class);
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        ResourceDescription location2 = session.describeResource(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = mapper.readValue(String.valueOf(location2.value), Point.class);
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        // Ignored provider shoudln't be there
        assertNull(session.describeProvider(ignoredProvider));
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
        byte[] csvContent = readFile("json/single.json");

        // Provider shouldn't exist yet
        assertNull(session.describeProvider(provider));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // Ensure value and type
        assertEquals(15, session.getResourceValue(provider, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp = Instant.from(LocalDateTime.of(2022, 12, 7, 15, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, session.describeResource(provider, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        final ObjectMapper mapper = new ObjectMapper();
        ResourceDescription location = session.describeResource(provider, "admin", "location");
        assertEquals(timestamp, location.timestamp);
        assertNotNull(location.value);
        Point geoPoint = mapper.readValue(String.valueOf(location.value), Point.class);
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
        byte[] csvContent = readFile("json/datetime.json");

        // Provider shouldn't exist yet
        assertNull(session.describeProvider(provider));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // Ensure timestamp
        Instant timestamp = Instant.from(LocalDateTime.of(2022, 12, 7, 12, 1, 58).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, session.describeResource(provider, "admin", "friendlyName").timestamp);
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
        byte[] csvContent = readFile("json/deep-multiple.json");

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // Check first provider
        assertEquals(0, session.getResourceValue(provider1, "data", type1 + "_value", Integer.class));

        // Ensure timestamp
        Instant timestamp = Instant.from(LocalDateTime.of(2021, 10, 26, 15, 28, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, session.describeResource(provider1, "data", type1 + "_value").timestamp);

        // Ensure location update (and its timestamp)
        final ObjectMapper mapper = new ObjectMapper();
        ResourceDescription location = session.describeResource(provider1, "admin", "location");
        assertEquals(timestamp, location.timestamp);
        assertNotNull(location.value);
        Point geoPoint = mapper.readValue(String.valueOf(location.value), Point.class);
        assertEquals(48.849577, geoPoint.coordinates.latitude, 0.001);
        assertEquals(2.350867, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        // Check 2nd provider
        assertEquals(7, session.getResourceValue(provider2, "data", type2 + "_value", Integer.class));

        // Ensure timestamp
        timestamp = Instant.from(LocalDateTime.of(2021, 10, 26, 15, 27, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp, session.describeResource(provider2, "data", type2 + "_value").timestamp);

        // Ensure location update (and its timestamp)
        location = session.describeResource(provider2, "admin", "location");
        assertEquals(timestamp, location.timestamp);
        assertNotNull(location.value);
        geoPoint = mapper.readValue(String.valueOf(location.value), Point.class);
        assertEquals(48.858396, geoPoint.coordinates.latitude, 0.001);
        assertEquals(2.350484, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));
    }
}

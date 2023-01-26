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
package org.eclipse.sensinact.gateway.southbound.device.parser.integration.csv;

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
public class CSVParserTest {

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
     * Mapping a CSV file without header
     */
    @Test
    void testNoHeader() throws Exception {
        // Excepted providers
        final String provider1 = "no-header-provider1";
        final String provider2 = "no-header-provider2";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("csv/csv-no-header-mapping.json");

        // Read the file
        byte[] csvContent = readFile("csv/csv-no-header.csv");

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // CSV loads strings by default
        assertEquals("42", session.getResourceValue(provider1, "data", "value", String.class));
        assertEquals("84", session.getResourceValue(provider2, "data", "value", String.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, session.describeResource(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, session.describeResource(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = (Point) location1.value;
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        ResourceDescription location2 = session.describeResource(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));
    }

    /**
     * Mapping a CSV file with header
     */
    @Test
    void testWithHeader() throws Exception {
        // Excepted providers
        final String provider1 = "header-provider1";
        final String provider2 = "header-provider2";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("csv/csv-header-mapping.json");

        // Read the file
        byte[] csvContent = readFile("csv/csv-header.csv");

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // CSV loads strings by default
        assertEquals("42", session.getResourceValue(provider1, "data", "value", String.class));
        assertEquals("84", session.getResourceValue(provider2, "data", "value", String.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, session.describeResource(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, session.describeResource(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = (Point) location1.value;
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        ResourceDescription location2 = session.describeResource(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));
    }

    /**
     * Tests type conversion
     */
    @Test
    void testTyped() throws Exception {
        // Excepted providers
        final String provider1 = "typed-provider1";
        final String provider2 = "typed-provider2";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("csv/csv-header-typed-mapping.json");

        // Read the file
        byte[] csvContent = readFile("csv/csv-header-typed.csv");

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        // Apply mapping
        deviceMapper.handle(config, csvContent);

        // Ensure resource type
        assertEquals(42, session.getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(84, session.getResourceValue(provider2, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, session.describeResource(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, session.describeResource(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = (Point) location1.value;
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        ResourceDescription location2 = session.describeResource(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));
    }
}

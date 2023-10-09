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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.parser.csv.CsvParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.util.promise.Promises;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests CSV-based mapping
 */
public class CSVParserTest {

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

        final CsvParser parser = new CsvParser();
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
        deviceMapper.addParser(cso, Map.of(IDeviceMappingParser.PARSER_ID, "csv"));
    }

    @AfterEach
    void stop() {
        deviceMapper.removeParser(cso, Map.of(IDeviceMappingParser.PARSER_ID, "csv"));
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
        byte[] fileContent = readFile("csv/csv-no-header.csv");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // CSV loads strings by default
        assertEquals("42", getResourceValue(provider1, "data", "value", String.class));
        assertEquals("84", getResourceValue(provider2, "data", "value", String.class));

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
        byte[] fileContent = readFile("csv/csv-header.csv");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // CSV loads strings by default
        assertEquals("42", getResourceValue(provider1, "data", "value", String.class));
        assertEquals("84", getResourceValue(provider2, "data", "value", String.class));

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
        byte[] fileContent = readFile("csv/csv-header-typed.csv");

        // Apply mapping
        deviceMapper.handle(config, Map.of(), fileContent);

        // Ensure resource type
        assertEquals(42, getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(84, getResourceValue(provider2, "data", "value", Integer.class));

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
    }

    /**
     * Tests literal
     */
    @Test
    void testLiteral() throws Exception {
        // Excepted providers
        final String provider1 = "literal-provider1";
        final String provider2 = "literal-provider2";
        final String literalProvider = "literal-provider";

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("csv/csv-literal-typed-mapping.json");

        for (final String fileName : List.of("csv-header-typed", "csv-literal")) {
            // Read the file
            byte[] rawfileContent = readFile("csv/" + fileName + ".csv");
            String fileContent = new String(rawfileContent, StandardCharsets.UTF_8);

            // Apply mapping
            deviceMapper.handle(config, Map.of(),
                    fileContent.replace("typed-", "literal-").getBytes(StandardCharsets.UTF_8));
        }

        // Ensure resource type
        assertEquals(42, getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(84, getResourceValue(provider2, "data", "value", Integer.class));
        assertEquals(21, getResourceValue(literalProvider, "data", "value", Integer.class));

        // Ensure valid constant value
        final String constantValue = "Grenoble";
        assertEquals(constantValue, getResourceValue(provider1, "sensor", "city", String.class));
        assertEquals(constantValue, getResourceValue(provider2, "sensor", "city", String.class));
        assertEquals(constantValue, getResourceValue(literalProvider, "sensor", "city", String.class));

        // Ensure default value
        final String defaultValue = "n/a";
        assertEquals(defaultValue, getResourceValue(provider1, "sensor", "street", String.class));
        assertEquals(defaultValue, getResourceValue(provider2, "sensor", "street", String.class));
        assertEquals("Cours Bériat", getResourceValue(literalProvider, "sensor", "street", String.class));
    }

    /**
     * Mapping a "CSV" file with only a single value and no header
     */
    @Test
    void testIsolatedValue() throws Exception {
        // Excepted provider
        final String provider = "isolated-value-" + String.valueOf(new Random().nextInt());

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("csv/isolated-value-mapping.json");

        // Read the file
        byte[] fileContent = readFile("csv/isolated-value.csv");

        // Apply mapping
        deviceMapper.handle(config, Map.of("provider", provider), fileContent);

        // CSV loads strings by default
        assertEquals("37.5", getResourceValue(provider, "data", "value", String.class));
    }

    /**
     * Mapping a "CSV" file with only a single value and no header, with a typed
     * mapping
     */
    @Test
    void testIsolatedValueTyped() throws Exception {
        // Excepted provider
        final String provider = "isolated-value-" + String.valueOf(new Random().nextInt());

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("csv/isolated-value-mapping-typed.json");

        // Read the file
        byte[] fileContent = readFile("csv/isolated-value.csv");

        // Apply mapping
        deviceMapper.handle(config, Map.of("provider", provider), fileContent);

        // CSV loads strings by default
        assertEquals(37.5f, getResourceValue(provider, "data", "value", Float.class));
    }

    /**
     * Mapping based on variables in multiple places
     */
    @Test
    void testVariables() throws Exception {
        // Excepted resource
        final String provider = "provider-vars-" + String.valueOf(new Random().nextInt());
        final String service = "svc-vars-" + String.valueOf(new Random().nextInt());
        final String resource = "rc-vars-" + String.valueOf(new Random().nextInt());

        // Read the configuration
        DeviceMappingConfigurationDTO config = readConfiguration("csv/csv-no-header-vars-mapping.json");

        // Read the file
        byte[] fileContent = readFile("csv/csv-no-header-vars.csv");

        // Apply mapping
        deviceMapper.handle(config, Map.of("provider", provider, "svc", service, "rc", resource), fileContent);

        assertEquals("3.4", getResourceValue(provider, service, "a", String.class));
        assertEquals("42", getResourceValue(provider, service, "b", String.class));
    }
}

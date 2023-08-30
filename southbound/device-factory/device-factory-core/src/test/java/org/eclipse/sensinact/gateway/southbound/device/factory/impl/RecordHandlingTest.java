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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.util.promise.Promises;

/**
 * Tests basic handling of a record
 */
public class RecordHandlingTest {

    private FactoryParserHandler deviceMapper;
    private ComponentServiceObjects<IDeviceMappingParser> cso;

    private FakeDeviceMappingParser parser;

    private List<BulkGenericDto> bulks = new ArrayList<>();

    @BeforeEach
    void start() throws InterruptedException {
        deviceMapper = new FactoryParserHandler();
        deviceMapper.prototypePush = Mockito.mock(DataUpdate.class);

        Mockito.when(deviceMapper.prototypePush.pushUpdate(Mockito.any())).thenAnswer(i -> {
            final BulkGenericDto dto = i.getArgument(0, BulkGenericDto.class);
            bulks.add(dto);
            return Promises.resolved(dto);
        });

        parser = new FakeDeviceMappingParser();
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
        deviceMapper.addParser(cso, Map.of(IDeviceMappingParser.PARSER_ID, "test"));
    }

    @AfterEach
    void stop() {
        deviceMapper.removeParser(cso, Map.of(IDeviceMappingParser.PARSER_ID, "test"));
        deviceMapper = null;
        cso = null;
        parser = null;
        bulks.clear();
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

    DeviceMappingConfigurationDTO prepareConfig() {
        DeviceMappingConfigurationDTO config = new DeviceMappingConfigurationDTO();
        config.parser = "test";
        config.parserOptions = new HashMap<>();
        config.mappingOptions = new DeviceMappingOptionsDTO();
        config.mapping = new HashMap<>();
        return config;
    }

    @Test
    void testNoProviderPlaceholder() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();
        try {
            deviceMapper.handle(config, Map.of(), new byte[0]);
            fail("Handler accepted an empty configuration");
        } catch (IllegalArgumentException e) {
            // OK
        }

        config.mapping.put("@model", "toto");
        config.mapping.put("@latitude", "toto");
        try {
            deviceMapper.handle(config, Map.of(), new byte[0]);
            fail("Handler accepted a configuration without a provider");
        } catch (IllegalArgumentException e) {
            // OK
        }

        config.mapping.put("@provider", "toto");
        deviceMapper.handle(config, Map.of(), new byte[0]);
        // OK
    }

    @Test
    void testBasicRecord() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        final String model = "testModel";
        final String provider = "testProvider";
        parser.setRecords(Map.of("m", model, "p", provider, "n", "name", "lat", 45f, "lon", 5f, "val", 42));

        // Test w/o a model
        config.mapping.put("@provider", "p");
        config.mapping.put("@name", "n");
        config.mapping.put("@latitude", "lat");
        config.mapping.put("@longitude", "lon");
        config.mapping.put("data/value", "val");
        deviceMapper.handle(config, Map.of(), new byte[0]);

        GenericDto dto = getResourceValue(provider, "admin", "friendlyName");
        assertEquals("name", dto.value);
        assertEquals(provider, dto.provider);
        assertEquals(provider, dto.model);
        assertEquals(42, getResourceValue(provider, "data", "value", Integer.class));
        Point location = getResourceValue(provider, "admin", "location", Point.class);
        assertEquals(45f, location.coordinates.latitude);
        assertEquals(5f, location.coordinates.longitude);

        // Test w/ a model
        bulks.clear();
        config.mapping.put("@model", "m");
        deviceMapper.handle(config, Map.of(), new byte[0]);
        dto = getResourceValue(provider, "admin", "friendlyName");
        assertEquals("name", dto.value);
        assertEquals(provider, dto.provider);
        assertEquals(model, dto.model);
    }

    @Test
    void testComplexRecord() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        final String model = "testModel";
        final String provider = "testProvider";
        parser.setRecords(Map.of("m", "someModel", "p", "someProvider", "n", "foo", "val", 21));

        // Test w/o a model
        config.mapping.put("@model", Map.of("literal", "${model}"));
        config.mapping.put("@provider", Map.of("literal", provider));
        config.mapping.put("@name", "n");
        config.mapping.put("$model", Map.of("literal", model));
        config.mapping.put("$value", Map.of("literal", 42, "type", "int"));
        config.mapping.put("$other", "n");
        config.mapping.put("data/text", Map.of("literal", "${other}=${value}"));
        config.mapping.put("data/value", Map.of("literal", "${value}", "type", "int"));
        deviceMapper.handle(config, Map.of(), new byte[0]);

        GenericDto dto = getResourceValue(provider, "admin", "friendlyName");
        assertEquals(model, dto.model);
        assertEquals(provider, dto.provider);
        assertEquals("foo", dto.value);
        assertEquals(42, getResourceValue(provider, "data", "value", Integer.class));
        assertEquals("foo=42", getResourceValue(provider, "data", "text", String.class));
    }

    @Test
    void testContext() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        final String model = "testModel";
        parser.setRecords(
                Map.of("m", model, "n", "name", "lat", 45f, "lon", 5f, "val", 42, "bar", "fizz", "foobar", "buzz"));

        // Test w/o a model
        config.mapping.put("@provider", "${context.foo}");
        config.mapping.put("@name", "n");
        config.mapping.put("@latitude", "lat");
        config.mapping.put("@longitude", "lon");
        config.mapping.put("data/value", "val");
        deviceMapper.handle(config, Map.of("foo", "bar"), new byte[0]);

        GenericDto dto = getResourceValue("fizz", "admin", "friendlyName");
        assertEquals("name", dto.value);
        assertEquals("fizz", dto.provider);
        assertEquals("fizz", dto.model);
        assertEquals(42, getResourceValue("fizz", "data", "value", Integer.class));
        Point location = getResourceValue("fizz", "admin", "location", Point.class);
        assertEquals(45f, location.coordinates.latitude);
        assertEquals(5f, location.coordinates.longitude);

        // Test w/ a model
        bulks.clear();
        config.mapping.put("@model", "m");
        deviceMapper.handle(config, Map.of("foo", "foobar"), new byte[0]);
        dto = getResourceValue("buzz", "admin", "friendlyName");
        assertEquals("name", dto.value);
        assertEquals("buzz", dto.provider);
        assertEquals(model, dto.model);
    }

    @Test
    void testNullAction() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        final Map<String, Object> record = new HashMap<>();
        record.put("p", "provider");
        record.put("nullVal", null);
        record.put("nonNullVal", 42);
        parser.setRecords(record);

        // Test default configuration (update)
        config.mapping.put("@provider", "p");
        config.mapping.put("data/val", "nonNullVal");
        config.mapping.put("data/null", "nullVal");
        deviceMapper.handle(config, Map.of(), new byte[0]);

        GenericDto dto = getResourceValue("provider", "data", "null");
        assertEquals(NullAction.UPDATE, dto.nullAction);
        assertNull(dto.value);

        dto = getResourceValue("provider", "data", "val");
        assertEquals(NullAction.UPDATE, dto.nullAction);
        assertEquals(42, dto.value);

        // Test ignore handling
        bulks.clear();
        config.mappingOptions.nullAction = NullAction.IGNORE;
        deviceMapper.handle(config, Map.of(), new byte[0]);

        dto = getResourceValue("provider", "data", "null");
        assertEquals(NullAction.IGNORE, dto.nullAction);
        assertNull(dto.value);

        dto = getResourceValue("provider", "data", "val");
        assertEquals(NullAction.IGNORE, dto.nullAction);
        assertEquals(42, dto.value);
    }
}

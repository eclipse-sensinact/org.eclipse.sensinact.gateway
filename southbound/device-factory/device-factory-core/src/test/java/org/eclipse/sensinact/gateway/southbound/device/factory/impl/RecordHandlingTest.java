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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
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
        deviceMapper.dataUpdate = Mockito.mock(DataUpdate.class);
        deviceMapper.metrics = Mockito.mock(IMetricsManager.class);

        Mockito.when(deviceMapper.dataUpdate.pushUpdate(Mockito.any())).thenAnswer(i -> {
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
                if (dto.provider.equals(provider) && dto.service.equals(service) && dto.resource.equals(resource)
                        && dto.metadata == null) {
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

    Object getResourceMetadata(final String provider, final String service, final String resource, final String key) {
        for (BulkGenericDto bulk : bulks) {
            for (GenericDto dto : bulk.dtos) {
                if (dto.provider.equals(provider) && dto.service.equals(service) && dto.resource.equals(resource)
                        && dto.metadata != null) {
                    return dto.metadata.get(key);
                }
            }
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
    void testMetadataRecord() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        final String model = "testModel";
        final String provider = "testProvider";
        parser.setRecords(Map.of("m", model, "p", provider, "n", "name", "lat", 45f, "lon", 5f, "val", 42, "foo", "bar",
                "unit", "A"));

        // Test w/o a model
        config.mapping.put("@provider", "p");
        config.mapping.put("@name", "n");
        config.mapping.put("@latitude", "lat");
        config.mapping.put("@longitude", "lon");
        config.mapping.put("data/value", "val");
        config.mapping.put("data/value/foo", "foo");
        config.mapping.put("data/value/unit", "unit");
        config.mapping.put("data/value/literal", Map.of("literal", "some-constant"));
        config.mapping.put("meta/no-data/foo", "foo");
        config.mapping.put("meta/no-data/unit", "unit");
        deviceMapper.handle(config, Map.of(), new byte[0]);

        // Check values
        assertEquals(42, getResourceValue(provider, "data", "value", Integer.class));
        assertEquals(null, getResourceValue(provider, "meta", "no-data", Object.class));

        // Check metadata
        assertEquals("bar", getResourceMetadata(provider, "data", "value", "foo"));
        assertEquals("A", getResourceMetadata(provider, "data", "value", "unit"));
        assertEquals("some-constant", getResourceMetadata(provider, "data", "value", "literal"));

        assertEquals("bar", getResourceMetadata(provider, "meta", "no-data", "foo"));
        assertEquals("A", getResourceMetadata(provider, "meta", "no-data", "unit"));
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
        config.mapping.put("data/nullVal", "nullVal");
        final Map<String, Object> mappingNullVal = new HashMap<>();
        mappingNullVal.put("path", "nullVal");
        mappingNullVal.put("type", "string");
        config.mapping.put("data/null", mappingNullVal);
        config.mapping.put("data/nullMapping", null);
        deviceMapper.handle(config, Map.of(), new byte[0]);

        GenericDto dto = getResourceValue("provider", "data", "null");
        assertEquals(NullAction.UPDATE, dto.nullAction);
        assertEquals(String.class, dto.type);
        assertNull(dto.value);

        dto = getResourceValue("provider", "data", "nullVal");
        assertEquals(NullAction.UPDATE, dto.nullAction);
        assertEquals(Object.class, dto.type);
        assertNull(dto.value);

        dto = getResourceValue("provider", "data", "nullMapping");
        assertEquals(NullAction.UPDATE, dto.nullAction);
        assertEquals(Object.class, dto.type);
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

    @Test
    void testVariableService() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        // Set a record
        final Map<String, Object> record = new HashMap<>();
        record.put("rc_provider", "provider");
        record.put("rc_name", "rc");
        record.put("rc_value", 42);
        parser.setRecords(record);

        // Set a context
        final Map<String, String> context = new HashMap<>();
        context.put("ctx", "svc");

        // Setup mapping
        config.mapping.put("@provider", "rc_provider");
        config.mapping.put("$service", "${context.ctx}");
        config.mapping.put("$resource", "rc_name");
        config.mapping.put("${service}/${resource}", "rc_value");

        // Parse
        deviceMapper.handle(config, context, new byte[0]);

        // Test values
        assertEquals(42, getResourceValue("provider", "svc", "rc").value);
    }

    @Test
    void testTimestamp() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        // Set a record
        final Instant now = Instant.now();
        final Map<String, Object> record = new HashMap<>();
        record.put("provider", "provider");
        record.put("timestamp_s", now.getEpochSecond());
        record.put("timestamp_ms", now.toEpochMilli());
        final long timestampNs = TimeUnit.SECONDS.toNanos(now.getEpochSecond()) + now.getNano();
        record.put("timestamp_ns", timestampNs);
        parser.setRecords(record);

        // Auto-compute timestamp format: seconds
        config.mapping.put("@provider", "provider");
        config.mapping.put("data/val", null);
        config.mapping.put("@timestamp", "timestamp_s");
        deviceMapper.handle(config, Map.of(), new byte[0]);
        GenericDto dto = getResourceValue("provider", "data", "val");
        assertEquals(Instant.ofEpochSecond(now.getEpochSecond()), dto.timestamp);

        // Auto-compute timestamp format: milliseconds
        bulks.clear();
        config.mapping.put("@timestamp", "timestamp_ms");
        deviceMapper.handle(config, Map.of(), new byte[0]);
        dto = getResourceValue("provider", "data", "val");
        assertEquals(Instant.ofEpochMilli(now.toEpochMilli()), dto.timestamp);

        // Auto-compute timestamp format: nanoseconds
        bulks.clear();
        config.mapping.put("@timestamp", "timestamp_ns");
        deviceMapper.handle(config, Map.of(), new byte[0]);
        dto = getResourceValue("provider", "data", "val");
        assertEquals(now, dto.timestamp);
    }

    @Test
    void testDateTime() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();

        // Set a record
        final Instant now = Instant.now();
        final OffsetDateTime date = now.atOffset(ZoneOffset.ofHours(1));
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM);
        final String strPattern = "MM dd, yyyy - Q - hh:mm a - s.n - X";

        final Map<String, Object> record = new HashMap<>();
        record.put("provider", "provider");
        record.put("iso", date.toString());
        record.put("custom", date.format(DateTimeFormatter.ofPattern(strPattern)));
        record.put("locale_fr", date.format(formatter.withLocale(Locale.FRANCE)));
        record.put("locale_de", date.format(formatter.withLocale(Locale.GERMAN)));
        record.put("locale_en", date.format(formatter.withLocale(Locale.UK)));
        record.put("locale_zh", date.format(formatter.withLocale(Locale.SIMPLIFIED_CHINESE)));
        parser.setRecords(record);

        // ISO format
        config.mapping.put("@provider", "provider");
        config.mapping.put("data/val", null);
        config.mapping.put("@datetime", "iso");
        config.mappingOptions.formatDateTime = null;
        config.mappingOptions.dateTimezone = null;
        deviceMapper.handle(config, Map.of(), new byte[0]);
        GenericDto dto = getResourceValue("provider", "data", "val");
        assertEquals(now, dto.timestamp);

        // Custom pattern
        bulks.clear();
        config.mapping.put("@datetime", "custom");
        config.mappingOptions.formatDateTime = strPattern;
        deviceMapper.handle(config, Map.of(), new byte[0]);
        dto = getResourceValue("provider", "data", "val");
        assertEquals(now, dto.timestamp);

        // Locale-based date (mix of country and language codes)
        for (Locale locale : List.of(Locale.FRANCE, Locale.GERMAN, Locale.UK, Locale.SIMPLIFIED_CHINESE)) {
            bulks.clear();
            config.mapping.put("@datetime", "locale_" + locale.getLanguage());
            config.mappingOptions.formatDateTime = null;
            config.mappingOptions.formatDateTimeLocale = locale.toString();
            config.mappingOptions.formatDateStyle = "long";
            config.mappingOptions.formatTimeStyle = "medium";
            config.mappingOptions.dateTimezone = "+01";
            deviceMapper.handle(config, Map.of(), new byte[0]);
            dto = getResourceValue("provider", "data", "val");
            assertEquals(now.truncatedTo(ChronoUnit.SECONDS), dto.timestamp);
        }
    }
}

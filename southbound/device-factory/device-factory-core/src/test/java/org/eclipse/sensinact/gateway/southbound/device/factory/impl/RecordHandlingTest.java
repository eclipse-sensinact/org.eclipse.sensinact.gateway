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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.MissingParserException;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.MappingPolicy;
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
        assertEquals(45f, location.coordinates().latitude());
        assertEquals(5f, location.coordinates().longitude());

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
        assertEquals(45f, location.coordinates().latitude());
        assertEquals(5f, location.coordinates().longitude());

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

    @Test
    void testHardcodedTimezone() throws Exception {
        final Map<String, Object> record = new HashMap<>();
        record.put("provider", "provider");
        record.put("nozone", "2026-08-11T10:46:10");
        parser.setRecords(record);

        final DeviceMappingConfigurationDTO config = prepareConfig();
        config.mapping.put("@provider", "provider");
        config.mapping.put("data/val", null);
        config.mapping.put("@datetime", "nozone");

        // Default timezone
        config.mappingOptions.formatDateTime = null;
        config.mappingOptions.dateTimezone = null;
        deviceMapper.handle(config, Map.of(), new byte[0]);
        GenericDto dtoZulu = getResourceValue("provider", "data", "val");
        assertEquals(Instant.parse("2026-08-11T10:46:10Z"), dtoZulu.timestamp);

        // Region timezone
        bulks.clear();
        config.mappingOptions.formatDateTime = "yyyy-MM-dd'T'HH:mm:ss";
        config.mappingOptions.dateTimezone = "Europe/Paris";
        deviceMapper.handle(config, Map.of(), new byte[0]);
        GenericDto dtoParis = getResourceValue("provider", "data", "val");
        assertEquals(Instant.parse("2026-08-11T08:46:10Z"), dtoParis.timestamp);

        // Offset timezone
        bulks.clear();
        config.mappingOptions.formatDateTime = "yyyy-MM-dd'T'HH:mm:ss";
        config.mappingOptions.dateTimezone = "+0200";
        deviceMapper.handle(config, Map.of(), new byte[0]);
        GenericDto dtoOffset = getResourceValue("provider", "data", "val");
        assertEquals(Instant.parse("2026-08-11T08:46:10Z"), dtoOffset.timestamp);
    }

    @Test
    void testTimezone() throws Exception {
        final Map<String, Object> record = new HashMap<>();
        record.put("provider", "provider");
        record.put("offset", "2026-08-11T10:46:10+0200");
        record.put("fullzone", "2026-08-11T11:57:22+0200[Europe/Paris]");
        parser.setRecords(record);

        final DeviceMappingConfigurationDTO config = prepareConfig();
        config.mapping.put("@provider", "provider");
        config.mapping.put("data/val", null);

        // Time offset
        config.mapping.put("@datetime", "offset");
        config.mappingOptions.formatDateTime = "yyyy-MM-dd'T'HH:mm:ssZ";
        config.mappingOptions.dateTimezone = null;
        deviceMapper.handle(config, Map.of(), new byte[0]);
        GenericDto dtoOffset = getResourceValue("provider", "data", "val");
        assertEquals(Instant.parse("2026-08-11T08:46:10Z"), dtoOffset.timestamp);

        // Full zone
        bulks.clear();
        config.mapping.put("@datetime", "fullzone");
        config.mappingOptions.formatDateTime = "yyyy-MM-dd'T'HH:mm:ssZ'['zzzz']'";
        config.mappingOptions.dateTimezone = null;
        deviceMapper.handle(config, Map.of(), new byte[0]);
        GenericDto dtoFullzone = getResourceValue("provider", "data", "val");
        assertEquals(Instant.parse("2026-08-11T09:57:22Z"), dtoFullzone.timestamp);
    }

    /**
     * Two records: a valid one for provider "ok" and the given one for provider
     * "test", both with a "data/value" integer entry and a "data/other" one
     */
    private void setPolicyRecords(final Map<String, Object> tested) {
        final Map<String, Object> valid = Map.of("p", "ok", "val", 1, "other", "a");
        final Map<String, Object> record = new HashMap<>(tested);
        record.put("p", "test");
        record.put("other", "b");
        parser.setRecords(List.of(new MapRecord(valid), new MapRecord(record)));
    }

    private DeviceMappingConfigurationDTO preparePolicyConfig(final Object valueMapping,
            final MappingPolicy... globalPolicies) {
        final DeviceMappingConfigurationDTO config = prepareConfig();
        config.mapping.put("@provider", "p");
        config.mapping.put("data/value", valueMapping);
        config.mapping.put("data/other", "other");
        config.mappingOptions.policies = List.of(globalPolicies);
        return config;
    }

    /**
     * Checks that the valid record was applied and the tested one rejected
     */
    private void assertTestedRecordRejected() {
        assertEquals(1, getResourceValue("ok", "data", "value", Integer.class));
        assertNull(getResourceValue("test", "data", "other"), "Record not rejected");
    }

    @Test
    void testMissingRequiredValueRejectsRecord() throws Exception {
        setPolicyRecords(Map.of());
        deviceMapper.handle(preparePolicyConfig(Map.of("path", "val", "type", "int")), Map.of(), new byte[0]);
        assertTestedRecordRejected();
    }

    @Test
    void testInvalidStrictValueRejectsRecord() throws Exception {
        setPolicyRecords(Map.of("val", "n/a"));
        deviceMapper.handle(preparePolicyConfig(Map.of("path", "val", "type", "int")), Map.of(), new byte[0]);
        assertTestedRecordRejected();
    }

    @Test
    void testOptionalMissingValueIgnored() throws Exception {
        setPolicyRecords(Map.of());
        deviceMapper.handle(preparePolicyConfig(Map.of("path", "val", "type", "int"), MappingPolicy.OPTIONAL),
                Map.of(), new byte[0]);
        assertEquals("b", getResourceValue("test", "data", "other", String.class));
        assertNull(getResourceValue("test", "data", "value"), "Ignored value sent");
    }

    @Test
    void testOptionalMissingValueWithoutDefaultIsNull() throws Exception {
        setPolicyRecords(Map.of());
        deviceMapper.handle(preparePolicyConfig(
                Map.of("path", "val", "type", "int", "policies", List.of("optional", "default"))), Map.of(),
                new byte[0]);
        final GenericDto dto = getResourceValue("test", "data", "value");
        assertNotNull(dto, "Default value not sent");
        assertNull(dto.value);
    }

    @Test
    void testLenientInvalidValueIgnored() throws Exception {
        setPolicyRecords(Map.of("val", "n/a"));
        deviceMapper.handle(preparePolicyConfig(Map.of("path", "val", "type", "int"), MappingPolicy.LENIENT),
                Map.of(), new byte[0]);
        assertEquals("b", getResourceValue("test", "data", "other", String.class));
        assertNull(getResourceValue("test", "data", "value"), "Ignored value sent");
    }

    @Test
    void testLenientInvalidValueTakesDefault() throws Exception {
        setPolicyRecords(Map.of("val", "n/a"));
        // The default key implies the default fallback
        deviceMapper.handle(preparePolicyConfig(
                Map.of("path", "val", "type", "int", "default", "0", "policies", List.of("lenient"))), Map.of(),
                new byte[0]);
        assertEquals(0, getResourceValue("test", "data", "value", Integer.class));
    }

    @Test
    void testDefaultKeyAppliesToMissingValue() throws Exception {
        setPolicyRecords(Map.of());
        // Mapping written before the policies: still valid
        deviceMapper.handle(preparePolicyConfig(Map.of("path", "val", "type", "int", "default", 7)), Map.of(),
                new byte[0]);
        assertEquals(7, getResourceValue("test", "data", "value", Integer.class));
    }

    @Test
    void testDefaultKeyKeptWithVariablePath() throws Exception {
        setPolicyRecords(Map.of());
        deviceMapper.handle(preparePolicyConfig(Map.of("path", "${context.field}", "type", "int", "default", 3)),
                Map.of("field", "missing"), new byte[0]);
        assertEquals(3, getResourceValue("test", "data", "value", Integer.class));
    }

    @Test
    void testExplicitNullIsNull() throws Exception {
        final Map<String, Object> record = new HashMap<>();
        record.put("val", null);
        setPolicyRecords(record);
        deviceMapper.handle(preparePolicyConfig(Map.of("path", "val", "type", "int", "default", 7)), Map.of(),
                new byte[0]);
        final GenericDto dto = getResourceValue("test", "data", "value");
        assertNotNull(dto, "Null value not sent");
        assertNull(dto.value, "Default value applied to an explicit null");
    }

    @Test
    void testEntryOverridesGlobalPoliciesByPair() throws Exception {
        final DeviceMappingConfigurationDTO config = preparePolicyConfig(
                Map.of("path", "val", "type", "int", "policies", List.of("required")), MappingPolicy.OPTIONAL,
                MappingPolicy.LENIENT);

        // The entry is required: a missing value rejects the record
        setPolicyRecords(Map.of());
        deviceMapper.handle(config, Map.of(), new byte[0]);
        assertTestedRecordRejected();

        // The entry keeps the global lenient policy
        bulks.clear();
        setPolicyRecords(Map.of("val", "n/a"));
        deviceMapper.handle(config, Map.of(), new byte[0]);
        assertEquals("b", getResourceValue("test", "data", "other", String.class));
        assertNull(getResourceValue("test", "data", "value"), "Ignored value sent");
    }

    @Test
    void testValidatePolicies() throws Exception {
        deviceMapper.validate(preparePolicyConfig(Map.of("path", "val", "policies", List.of("Optional", "DEFAULT")),
                MappingPolicy.LENIENT));

        assertThrows(IllegalArgumentException.class, () -> deviceMapper
                .validate(preparePolicyConfig(Map.of("path", "val", "policies", List.of("optional", "required")))));
        assertThrows(IllegalArgumentException.class,
                () -> deviceMapper.validate(preparePolicyConfig(Map.of("path", "val", "policies", List.of("lax")))));
        assertThrows(IllegalArgumentException.class,
                () -> deviceMapper.validate(preparePolicyConfig(Map.of("path", "val", "policies", "optional"))));
        assertThrows(IllegalArgumentException.class, () -> deviceMapper
                .validate(preparePolicyConfig("val", MappingPolicy.STRICT, MappingPolicy.LENIENT)));

        // Not a policy matter: still checked when a payload is handled
        final DeviceMappingConfigurationDTO noParser = preparePolicyConfig("val");
        noParser.parser = null;
        deviceMapper.validate(noParser);
    }

    @Test
    void testArrayInPlaceOfAnObjectRejectsOnlyItsRecord() throws Exception {
        final DeviceMappingConfigurationDTO config = prepareConfig();
        config.mapping.put("@provider", "p");
        config.mapping.put("data/value", "d/h");
        parser.setRecords(List.of(new MapRecord(Map.of("p", "ok", "d", Map.of("h", 1))),
                new MapRecord(Map.of("p", "test", "d", List.of(1, 2)))));

        deviceMapper.handle(config, Map.of(), new byte[0]);
        assertEquals(1, getResourceValue("ok", "data", "value", Integer.class));
        assertNull(getResourceValue("test", "data", "value"), "Record not rejected");
    }

    @Test
    void testUnusableConfigurationIsCounted() throws Exception {
        final IMetricCounter counter = Mockito.mock(IMetricCounter.class);
        Mockito.when(deviceMapper.metrics.getCounter("device.factory.error.configuration")).thenReturn(counter);

        final DeviceMappingConfigurationDTO config = preparePolicyConfig("val");
        config.parser = "unknown";
        assertThrows(MissingParserException.class, () -> deviceMapper.handle(config, Map.of(), new byte[0]));
        Mockito.verify(counter).inc();
    }

    @Test
    void testRejectionsAreCounted() throws Exception {
        final IMetricCounter counter = Mockito.mock(IMetricCounter.class);
        Mockito.when(deviceMapper.metrics.getCounter("device.factory.error.missing_value")).thenReturn(counter);

        setPolicyRecords(Map.of());
        final DeviceMappingConfigurationDTO config = preparePolicyConfig(Map.of("path", "val", "type", "int"));
        deviceMapper.handle(config, Map.of(), new byte[0]);
        deviceMapper.handle(config, Map.of(), new byte[0]);
        Mockito.verify(counter, Mockito.times(2)).inc();
    }

    @Test
    void testMetadataEntryPolicies() throws Exception {
        setPolicyRecords(Map.of());
        final DeviceMappingConfigurationDTO config = preparePolicyConfig("val");
        config.mapping.put("data/value/unit", Map.of("path", "unit", "default", "%"));

        // The default key makes the metadata optional
        deviceMapper.handle(config, Map.of(), new byte[0]);
        assertEquals("%", getResourceMetadata("ok", "data", "value", "unit"));

        // Required by default: the record is rejected
        bulks.clear();
        config.mapping.put("data/value/unit", "unit");
        deviceMapper.handle(config, Map.of(), new byte[0]);
        assertNull(getResourceValue("ok", "data", "value"), "Record not rejected");
    }

    @Test
    void testNullActionAppliesToAnExplicitNull() throws Exception {
        final Map<String, Object> record = new HashMap<>();
        record.put("val", null);
        setPolicyRecords(record);
        final DeviceMappingConfigurationDTO config = preparePolicyConfig(Map.of("path", "val", "type", "int"));
        config.mappingOptions.nullAction = NullAction.IGNORE;

        deviceMapper.handle(config, Map.of(), new byte[0]);
        final GenericDto dto = getResourceValue("test", "data", "value");
        assertNotNull(dto, "Null value not sent");
        assertNull(dto.value);
        assertEquals(NullAction.IGNORE, dto.nullAction);
    }

    @Test
    void testValidateDefaultsAndUnsupportedPolicies() throws Exception {
        // A default value that can't be converted to the entry type
        assertThrows(IllegalArgumentException.class, () -> deviceMapper
                .validate(preparePolicyConfig(Map.of("path", "val", "type", "int", "default", "n/a"))));

        // Policies on a place holder, a variable or a literal
        final DeviceMappingConfigurationDTO placeholder = preparePolicyConfig("val");
        placeholder.mapping.put("@name", Map.of("path", "n", "policies", List.of("optional")));
        assertThrows(IllegalArgumentException.class, () -> deviceMapper.validate(placeholder));

        final DeviceMappingConfigurationDTO variable = preparePolicyConfig("val");
        variable.mapping.put("$v", Map.of("path", "n", "policies", List.of("optional")));
        assertThrows(IllegalArgumentException.class, () -> deviceMapper.validate(variable));

        assertThrows(IllegalArgumentException.class, () -> deviceMapper.validate(
                preparePolicyConfig(Map.of("literal", "x", "policies", List.of("optional")))));

        // A resource named after a variable is not a variable
        final DeviceMappingConfigurationDTO named = preparePolicyConfig("val");
        named.mapping.put("${context.svc}/data", Map.of("path", "val", "policies", List.of("optional")));
        deviceMapper.validate(named);

        // The provider is required
        final DeviceMappingConfigurationDTO noProvider = preparePolicyConfig("val");
        noProvider.mapping.remove("@provider");
        assertThrows(IllegalArgumentException.class, () -> deviceMapper.validate(noProvider));
    }

    @Test
    void testNullMappingOptions() throws Exception {
        setPolicyRecords(Map.of("val", 2));
        final DeviceMappingConfigurationDTO config = preparePolicyConfig(Map.of("path", "val", "type", "int"));
        config.mappingOptions = null;

        deviceMapper.validate(config);
        deviceMapper.handle(config, Map.of(), new byte[0]);
        assertEquals(2, getResourceValue("test", "data", "value", Integer.class));
    }

    @Test
    void testPoliciesOnAVariableNamedResource() throws Exception {
        setPolicyRecords(Map.of());
        final DeviceMappingConfigurationDTO config = preparePolicyConfig("val");
        config.mapping.remove("data/value");
        config.mapping.put("${context.svc}/value", Map.of("path", "val", "type", "int", "policies", List.of("optional")));

        deviceMapper.handle(config, Map.of("svc", "data"), new byte[0]);
        // Missing but optional: the record is applied without the value
        assertEquals("b", getResourceValue("test", "data", "other", String.class));
        assertNull(getResourceValue("test", "data", "value"));
    }

    @Test
    void testUnparseableDateRejectsOnlyItsRecord() throws Exception {
        // Worked example 3 of the specification, KP-89
        final DeviceMappingConfigurationDTO config = prepareConfig();
        config.mapping.put("@provider", "p");
        config.mapping.put("@datetime", "time");
        config.mapping.put("data/value", "val");
        parser.setRecords(List.of(new MapRecord(Map.of("p", "IST-0001", "time", "2026-10-12T08:00:00Z", "val", 1)),
                new MapRecord(Map.of("p", "IST-0002", "time", "not-a-date", "val", 2))));

        deviceMapper.handle(config, Map.of(), new byte[0]);
        assertEquals(1, getResourceValue("IST-0001", "data", "value", Integer.class));
        assertNull(getResourceValue("IST-0002", "data", "value"), "Record with a bad date applied");
    }
}

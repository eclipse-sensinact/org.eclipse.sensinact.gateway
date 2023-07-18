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
package org.eclipse.sensinact.prototype.extract.impl;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class GenericDtoExtractorTest {

    private static final String PROVIDER = "provider";
    private static final String PROVIDER_2 = "provider2";
    private static final String SERVICE = "service";
    private static final String SERVICE_2 = "service2";
    private static final String RESOURCE = "resource";
    private static final String RESOURCE_2 = "resource2";

    private static final Integer VALUE = 5;
    private static final String VALUE_2 = "Fourteen";

    private static final String METADATA_KEY = "foo";
    private static final String METADATA_KEY_2 = "bar";
    private static final String METADATA_VALUE = "fizz";
    private static final Integer METADATA_VALUE_2 = 42;

    /**
     * The Provider, Service and Resource mappings must be identifiable for all data
     * and metadata values
     */
    @Nested
    class MissingIdentity {
        @Test
        void missingProvider() {
            Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
                extractor().getUpdates(makeTestDto(null, SERVICE, RESOURCE, VALUE, null, null));
            });

            assertTrue(thrown.getMessage().contains("provider"), "Wrong message: " + thrown.getMessage());
        }

        @Test
        void missingService() {
            Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
                extractor().getUpdates(makeTestDto(PROVIDER, null, RESOURCE, VALUE, null, null));
            });

            assertTrue(thrown.getMessage().contains("service"), "Wrong message: " + thrown.getMessage());
        }

        @Test
        void missingResource() {
            Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
                extractor().getUpdates(makeTestDto(PROVIDER, SERVICE, null, VALUE, null, null));
            });

            assertTrue(thrown.getMessage().contains("resource"), "Wrong message: " + thrown.getMessage());
        }
    }

    /**
     * Data values of different types should be processed into {@link DataUpdateDto}
     * instances
     */
    @Nested
    class DataValues {
        @Test
        void integerValue() {
            List<? extends AbstractUpdateDto> updates = extractor()
                    .getUpdates(makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE, null, null));

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertNull(dud.type);
        }

        @Test
        void integerValueWithTimestamp() {
            Instant timestamp = Instant.ofEpochMilli(1234567890L);
            List<? extends AbstractUpdateDto> updates = extractor()
                    .getUpdates(makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE, null, null, timestamp));

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted, timestamp);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertNull(dud.type);
        }

        @Test
        void integerValueWithLongType() {
            List<? extends AbstractUpdateDto> updates = extractor()
                    .getUpdates(makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE, Long.class, null));

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertEquals(Long.class, dud.type);
        }

        @Test
        void stringValue() {
            List<? extends AbstractUpdateDto> updates = extractor()
                    .getUpdates(makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE_2, null, null));

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertNull(dud.type);
        }
    }

    /**
     * Metadata values should be processed into {@link MetadataUpdateDto} instances
     */
    @Nested
    class MetaDataValues {
        @Test
        void singleValue() {
            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(
                    makeTestDto(PROVIDER, SERVICE, RESOURCE, null, null, singletonMap(METADATA_KEY, METADATA_VALUE)));

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud.metadata);
            assertTrue(dud.removeNullValues, "Null values should be removed");
            assertFalse(dud.removeMissingValues, "Missing values should be kept");
        }

        @Test
        void multiValues() {
            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(makeTestDto(PROVIDER, SERVICE, RESOURCE,
                    null, null, Map.of(METADATA_KEY, METADATA_VALUE, METADATA_KEY_2, METADATA_VALUE_2)));

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud = (MetadataUpdateDto) extracted;

            assertEquals(Map.of(METADATA_KEY, METADATA_VALUE, METADATA_KEY_2, METADATA_VALUE_2), dud.metadata);
            assertTrue(dud.removeNullValues, "Null values should be removed");
            assertFalse(dud.removeMissingValues, "Missing values should be kept");
        }
    }

    /**
     * An input which defines data and metadata must be transformed into the
     * appropriate dtos
     */
    @Nested
    class CombinedDataAndMetadata {
        @Test
        void singleValue() {
            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(
                    makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE, null, singletonMap(METADATA_KEY, METADATA_VALUE)));

            assertEquals(2, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertNull(dud.type);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
        }

        @Test
        void multiValues() {
            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(
                    makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE, null, singletonMap(METADATA_KEY, METADATA_VALUE)));

            assertEquals(2, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertNull(dud.type);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
        }
    }

    /**
     * The {@link BulkGenericDto} must be usable for input of multiple values
     */
    @Nested
    class BulkExtraction {
        @Test
        void multiValues() {

            BulkGenericDto dto = new BulkGenericDto();

            dto.dtos = List.of(
                    makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE, null, singletonMap(METADATA_KEY, METADATA_VALUE)),
                    makeTestDto(PROVIDER_2, SERVICE_2, RESOURCE_2, VALUE_2, null,
                            singletonMap(METADATA_KEY_2, METADATA_VALUE_2)));

            List<? extends AbstractUpdateDto> updates = multiExtractor().getUpdates(dto);

            assertEquals(4, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertNull(dud.type);

            extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER_2.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted, false);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertNull(dud.type);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER_2.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted, false);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY_2, METADATA_VALUE_2), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
        }
    }

    private void checkCommonFields(AbstractUpdateDto extracted) {
        checkCommonFields(extracted, true);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, boolean use1) {
        checkCommonFields(extracted, use1, Instant.now(), false);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, Instant timestamp) {
        checkCommonFields(extracted, true, timestamp, true);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, boolean use1, Instant timestamp,
            boolean exactTimestamp) {
        assertEquals(use1 ? PROVIDER : PROVIDER_2, extracted.provider);
        assertEquals(use1 ? SERVICE : SERVICE_2, extracted.service);
        assertEquals(use1 ? RESOURCE : RESOURCE_2, extracted.resource);
        if (exactTimestamp) {
            assertEquals(extracted.timestamp, timestamp);
        } else {
            assertTrue(Duration.between(extracted.timestamp, timestamp).minusMillis(100).isNegative(),
                    () -> "The timestamp was not set properly got: " + extracted.timestamp + " used was: " + timestamp);
        }
    }

    private GenericDtoDataExtractor extractor() {
        return new GenericDtoDataExtractor();
    }

    private BulkGenericDtoDataExtractor multiExtractor() {
        return new BulkGenericDtoDataExtractor();
    }

    private GenericDto makeTestDto(String provider, String service, String resource, Object data, Class<?> dataType,
            Map<String, Object> metadata) {
        return makeTestDto(provider, service, resource, data, dataType, metadata, null);
    }

    private GenericDto makeTestDto(String provider, String service, String resource, Object data, Class<?> dataType,
            Map<String, Object> metadata, Instant timestamp) {
        GenericDto dto = new GenericDto();
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = data;
        dto.type = dataType;
        dto.metadata = metadata;
        dto.timestamp = timestamp;
        return dto;
    }
}

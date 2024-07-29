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
package org.eclipse.sensinact.core.extract.impl;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.annotation.dto.DuplicateAction;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.core.dto.impl.FailedMappingDto;
import org.eclipse.sensinact.core.dto.impl.MetadataUpdateDto;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
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
            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(makeTestDto(null, SERVICE, RESOURCE, VALUE, null, null));
            assertEquals(1, updates.size());
            AbstractUpdateDto aud = updates.get(0);
            assertNull(aud.provider);
            assertEquals(SERVICE, aud.service);
            assertEquals(RESOURCE, aud.resource);

            assertInstanceOf(FailedMappingDto.class, aud);
            FailedMappingDto fmd = (FailedMappingDto) aud;
            assertNotNull(fmd.mappingFailure);
            assertTrue(fmd.mappingFailure.getMessage().contains("provider"), "Wrong message: " + fmd.mappingFailure.getMessage());
        }

        @Test
        void missingService() {
            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(makeTestDto(PROVIDER, null, RESOURCE, VALUE, null, null));
            assertEquals(1, updates.size());
            AbstractUpdateDto aud = updates.get(0);
            assertEquals(PROVIDER, aud.provider);
            assertNull(aud.service);
            assertEquals(RESOURCE, aud.resource);

            assertInstanceOf(FailedMappingDto.class, aud);
            FailedMappingDto fmd = (FailedMappingDto) aud;
            assertNotNull(fmd.mappingFailure);
            assertTrue(fmd.mappingFailure.getMessage().contains("service"), "Wrong message: " + fmd.mappingFailure.getMessage());
        }

        @Test
        void missingResource() {
            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(makeTestDto(PROVIDER, SERVICE, null, VALUE, null, null));
            assertEquals(1, updates.size());
            AbstractUpdateDto aud = updates.get(0);
            assertEquals(PROVIDER, aud.provider);
            assertEquals(SERVICE, aud.service);
            assertNull(aud.resource);

            assertInstanceOf(FailedMappingDto.class, aud);
            FailedMappingDto fmd = (FailedMappingDto) aud;
            assertNotNull(fmd.mappingFailure);
            assertTrue(fmd.mappingFailure.getMessage().contains("resource"), "Wrong message: " + fmd.mappingFailure.getMessage());
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
            assertEquals(NullAction.IGNORE, dud.actionOnNull);
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);
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
            assertEquals(NullAction.IGNORE, dud.actionOnNull);
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);
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
            assertEquals(NullAction.IGNORE, dud.actionOnNull);
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);
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
            assertEquals(NullAction.IGNORE, dud.actionOnNull);
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);
        }

        @Test
        void nullAction() {
            GenericDto testDto = makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE_2, null, null);
            testDto.nullAction = NullAction.UPDATE;
            List<? extends AbstractUpdateDto> updates = extractor()
                    .getUpdates(testDto);

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertNull(dud.type);
            assertEquals(NullAction.UPDATE, dud.actionOnNull);
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);
        }

        @Test
        void duplicateAction() {
            GenericDto testDto = makeTestDto(PROVIDER, SERVICE, RESOURCE, VALUE_2, null, null);
            testDto.duplicateDataAction = DuplicateAction.UPDATE_IF_DIFFERENT;
            List<? extends AbstractUpdateDto> updates = extractor()
                    .getUpdates(testDto);

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertNull(dud.type);
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud.actionOnDuplicate);
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
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud.actionOnDuplicate);
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
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud.actionOnDuplicate);
        }

        @Test
        void nullAction() {
            GenericDto testDto = makeTestDto(PROVIDER, SERVICE, RESOURCE, null, null, singletonMap(METADATA_KEY, METADATA_VALUE));
            testDto.nullAction = NullAction.UPDATE;

            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(testDto);

            assertEquals(2, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);
            assertTrue(extracted instanceof DataUpdateDto, "Not a metadata update dto " + extracted.getClass());
            assertEquals(NullAction.UPDATE, extracted.actionOnNull);

            extracted = updates.get(1);
            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud.metadata);
            assertTrue(dud.removeNullValues, "Null values should be removed");
            assertFalse(dud.removeMissingValues, "Missing values should be kept");
            assertEquals(NullAction.UPDATE, dud.actionOnNull);
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud.actionOnDuplicate);
        }

        @Test
        void duplicateValue() {
            GenericDto testDto = makeTestDto(PROVIDER, SERVICE, RESOURCE, null, null, singletonMap(METADATA_KEY, METADATA_VALUE));
            testDto.duplicateMetadataAction = DuplicateAction.UPDATE_ALWAYS;

            List<? extends AbstractUpdateDto> updates = extractor().getUpdates(
                    testDto);

            assertEquals(1, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.get(0);

            checkCommonFields(extracted);

            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud.metadata);
            assertTrue(dud.removeNullValues, "Null values should be removed");
            assertFalse(dud.removeMissingValues, "Missing values should be kept");
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);
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
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud2.actionOnDuplicate);
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
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud2.actionOnDuplicate);
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

            dto.dtos.get(1).duplicateDataAction = DuplicateAction.UPDATE_IF_DIFFERENT;
            dto.dtos.get(1).duplicateMetadataAction = DuplicateAction.UPDATE_ALWAYS;

            List<? extends AbstractUpdateDto> updates = multiExtractor().getUpdates(dto);

            assertEquals(4, updates.size(), "Wrong number of updates " + updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertNull(dud.type);
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud.actionOnDuplicate);

            extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER_2.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted, false);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertNull(dud.type);
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud.actionOnDuplicate);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
            assertEquals(DuplicateAction.UPDATE_IF_DIFFERENT, dud2.actionOnDuplicate);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance)
                    .filter(d -> PROVIDER_2.equals(d.provider)).findFirst().get();

            checkCommonFields(extracted, false);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY_2, METADATA_VALUE_2), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
            assertEquals(DuplicateAction.UPDATE_ALWAYS, dud2.actionOnDuplicate);
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

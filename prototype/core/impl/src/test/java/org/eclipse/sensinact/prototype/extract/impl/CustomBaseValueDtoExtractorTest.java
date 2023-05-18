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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.core.push.dto.BaseValueDto;
import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.NullAction;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Resource;
import org.eclipse.sensinact.prototype.annotation.dto.Service;
import org.eclipse.sensinact.prototype.annotation.dto.Timestamp;
import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CustomBaseValueDtoExtractorTest {

    private static final String PROVIDER = "provider";
    private static final String PROVIDER_2 = "provider2";
    private static final String SERVICE = "service";
    private static final String SERVICE_2 = "service2";
    private static final String RESOURCE = "resource";
    private static final String RESOURCE_2 = "resource2";

    private static final Integer VALUE = 5;
    private static final String VALUE_2 = "Fourteen";

    private static final String METADATA_KEY = "foo";
    private static final String METADATA_VALUE = "fizz";

    public static class BasicDto extends BaseValueDto {
        @Data
        public String foo;
    }

    public static class ClashingDto extends BaseValueDto {
        @Data
        public String foo;

        @Data
        public Integer bar;
    }

    public static class ExtendedDto extends BaseValueDto {

        @Data
        public String foo;

        @Provider(PROVIDER_2)
        @Service(SERVICE_2)
        @Resource(RESOURCE_2)
        @Data(type = Long.class)
        public Integer bar;

        @Resource("null")
        @Data(onNull = NullAction.UPDATE)
        public String nullable;

        @Timestamp(ChronoUnit.SECONDS)
        public Long time;
    }

    /**
     * Tests for a {@link BaseValueDto} dto subclass with a single data field
     */
    @Nested
    public class BaseValueDtoTests {

        @Test
        void basicDto() {
            BasicDto dto = new BasicDto();
            populate(dto);

            dto.foo = VALUE_2;

            List<? extends AbstractUpdateDto> updates = extractor(BasicDto.class).getUpdates(dto);

            assertEquals(2, updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertEquals(String.class, dud.type);

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
     * Tests with clashing mappings for the same resource
     */
    @Nested
    class ClashingMappingsTest {
        @Test
        void clashingDtoWithFoo() {
            ClashingDto dto = new ClashingDto();
            populate(dto);

            dto.foo = VALUE_2;

            List<? extends AbstractUpdateDto> updates = extractor(ClashingDto.class).getUpdates(dto);

            assertEquals(2, updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertEquals(String.class, dud.type);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
        }

        @Test
        void clashingDtoWithBar() {
            ClashingDto dto = new ClashingDto();
            populate(dto);

            dto.bar = VALUE;

            List<? extends AbstractUpdateDto> updates = extractor(ClashingDto.class).getUpdates(dto);

            assertEquals(2, updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertEquals(Integer.class, dud.type);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");
        }

        @Test
        void clashingDtoWithFooAndBar() {
            ClashingDto dto = new ClashingDto();
            populate(dto);

            dto.foo = VALUE_2;
            dto.bar = VALUE;

            List<? extends AbstractUpdateDto> updates = extractor(ClashingDto.class).getUpdates(dto);

            assertEquals(3, updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> String.class == ((DataUpdateDto) d).type).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertEquals(String.class, dud.type);

            extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> Integer.class == ((DataUpdateDto) d).type).findFirst().get();

            checkCommonFields(extracted);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertEquals(Integer.class, dud.type);

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
     * Tests for mapping multiple custom data fields
     */
    @Nested
    class CustomDataFields {
        @Test
        void extendedDtoMapping() {
            Instant time = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

            ExtendedDto dto = new ExtendedDto();
            populate(dto);

            dto.foo = VALUE_2;
            dto.bar = VALUE;
            dto.time = time.getEpochSecond();

            List<? extends AbstractUpdateDto> updates = extractor(ExtendedDto.class).getUpdates(dto);

            assertEquals(4, updates.size());

            AbstractUpdateDto extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> RESOURCE.equals(d.resource)).findFirst().get();

            checkCommonFields(extracted, time);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            DataUpdateDto dud = (DataUpdateDto) extracted;

            assertEquals(VALUE_2, dud.data);
            assertEquals(String.class, dud.type);

            extracted = updates.stream().filter(DataUpdateDto.class::isInstance)
                    .filter(d -> RESOURCE_2.equals(d.resource)).findFirst().get();

            checkCommonFields(extracted, false, time);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            dud = (DataUpdateDto) extracted;

            assertEquals(VALUE, dud.data);
            assertEquals(Long.class, dud.type);

            extracted = updates.stream().filter(DataUpdateDto.class::isInstance).filter(d -> "null".equals(d.resource))
                    .findFirst().get();

            checkCommonFields(extracted, "null", time);

            assertTrue(extracted instanceof DataUpdateDto, "Not a data update dto " + extracted.getClass());

            dud = (DataUpdateDto) extracted;

            assertNull(dud.data);
            assertEquals(String.class, dud.type);

            extracted = updates.stream().filter(MetadataUpdateDto.class::isInstance).findFirst().get();

            checkCommonFields(extracted, time);
            assertTrue(extracted instanceof MetadataUpdateDto, "Not a metadata update dto " + extracted.getClass());

            MetadataUpdateDto dud2 = (MetadataUpdateDto) extracted;

            assertEquals(singletonMap(METADATA_KEY, METADATA_VALUE), dud2.metadata);
            assertTrue(dud2.removeNullValues, "Null values should be removed");
            assertFalse(dud2.removeMissingValues, "Missing values should be kept");

        }

    }

    DataExtractor extractor(Class<?> clazz) {
        return new CustomDtoDataExtractor(clazz);
    }

    void populate(BaseValueDto bvd) {
        bvd.provider = PROVIDER;
        bvd.service = SERVICE;
        bvd.resource = RESOURCE;
        bvd.metadata = Collections.singletonMap(METADATA_KEY, METADATA_VALUE);
    }

    private void checkCommonFields(AbstractUpdateDto extracted) {
        checkCommonFields(extracted, true);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, Instant time) {
        checkCommonFields(extracted, true, time);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, String altResource, Instant time) {
        checkCommonFields(extracted, true, altResource, time);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, boolean use1) {
        checkCommonFields(extracted, use1, null);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, boolean use1, Instant time) {
        checkCommonFields(extracted, use1, null, time);
    }

    private void checkCommonFields(AbstractUpdateDto extracted, boolean use1, String altResource, Instant time) {
        assertEquals(use1 ? PROVIDER : PROVIDER_2, extracted.provider);
        assertEquals(use1 ? SERVICE : SERVICE_2, extracted.service);
        assertEquals(altResource == null ? use1 ? RESOURCE : RESOURCE_2 : altResource, extracted.resource);
        if (time == null) {
            assertTrue(Duration.between(extracted.timestamp, Instant.now()).minusMillis(100).isNegative(),
                    () -> "The timestamp was not set properly got: " + extracted.timestamp + " now is: "
                            + Instant.now());
        } else {
            assertEquals(time, extracted.timestamp);
        }
    }
}

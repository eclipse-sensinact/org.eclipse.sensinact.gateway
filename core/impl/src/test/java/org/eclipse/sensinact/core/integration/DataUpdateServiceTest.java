/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.DuplicateAction;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.DataUpdateException;
import org.eclipse.sensinact.core.push.FailedUpdatesException;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Tests the error behavior of the {@link DataUpdate} service
 */
public class DataUpdateServiceTest {

    private static final String PROVIDER = "DataUpdateServiceTestProvider";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";

    @InjectService
    DataUpdate push;
    @InjectService
    GatewayThread gt;

    @AfterEach
    void stop(@InjectService GatewayThread gt) throws InvocationTargetException, InterruptedException {
        gt.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                Optional.ofNullable(twin.getProvider(PROVIDER)).ifPresent(SensinactProvider::delete);
                return pf.resolved(null);
            }
        }).getValue();
    }

    private Integer getResourceValue() throws Exception {
        return gt.execute(new ResourceCommand<Integer>(PROVIDER, SERVICE, RESOURCE) {

            @Override
            protected Promise<Integer> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getValue().map(t -> (Integer) t.getValue());
            }
        }).getValue();
    }

    private TimedValue<Integer> getResourceTimedValue() throws Exception {
        return gt.execute(new ResourceCommand<TimedValue<Integer>>(PROVIDER, SERVICE, RESOURCE) {

            @Override
            protected Promise<TimedValue<Integer>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getValue(Integer.class);
            }
        }).getValue();
    }

    private Object getResourceMetadataValue() throws Exception {
        return getResourceMetadataValue(PROVIDER);
    }

    private Object getResourceMetadataValue(final String provider) throws Exception {
        return getResourceMetadataTimedValue(provider).getValue();
    }

    private TimedValue<Object> getResourceMetadataTimedValue() throws Exception {
        return getResourceMetadataTimedValue(PROVIDER);
    }

    private TimedValue<Object> getResourceMetadataTimedValue(final String provider) throws Exception {
        return gt.execute(new ResourceCommand<TimedValue<Object>>(provider, SERVICE, RESOURCE) {

            @Override
            protected Promise<TimedValue<Object>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMetadataValue("foo");
            }
        }).getValue();
    }

    public static class AnnotatedDTO {
        @Provider
        public String provider;

        @Service
        public String service;

        @Resource
        public String resource;

        @Timestamp
        public String time;

        @Data(type = Integer.class)
        public String data;
    }

    @Provider(PROVIDER)
    @Service(SERVICE)
    @Resource(RESOURCE)
    public static abstract class AnnotatedMetadataDto {
        @Timestamp
        public Instant time;
    }

    public static class DefaultMetadataDto extends AnnotatedMetadataDto {
        @Metadata
        public String foo;
    }

    public static class DuplicateMetadataDto extends AnnotatedMetadataDto {
        @Metadata(onDuplicate = DuplicateAction.UPDATE_ALWAYS)
        public String foo;
    }

    public static class NullMetadataDto extends AnnotatedMetadataDto {
        @Metadata(onNull = NullAction.UPDATE)
        public String foo;
    }

    /**
     * Tests valid admin resource creation with provider and update
     */
    @Nested
    class ValidPushes {
        @Test
        void testSimplePush() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            push.pushUpdate(dto).getValue();

            assertEquals(42, getResourceValue());
        }

        @Test
        void testSimplePushUpdate() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = null;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            dto.nullAction = NullAction.UPDATE;
            push.pushUpdate(dto).getValue();

            TimedValue<Integer> tv = getResourceTimedValue();
            assertNull(tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.value = 42;
            push.pushUpdate(dto).getValue();
            assertEquals(42, getResourceValue());

            dto.value = null;
            push.pushUpdate(dto).getValue();
            tv = getResourceTimedValue();
            assertNull(tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());
        }

        @Test
        void testSimplePushUpdateIgnore() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            dto.nullAction = NullAction.IGNORE;
            push.pushUpdate(dto).getValue();

            push.pushUpdate(dto).getValue();

            dto.value = null;
            push.pushUpdate(dto).getValue();
            assertEquals(42, getResourceValue());
        }

        @Test
        void testSimplePushUpdateIfPresent() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = null;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            dto.nullAction = NullAction.UPDATE_IF_PRESENT;
            push.pushUpdate(dto).getValue();

            TimedValue<Integer> tv = getResourceTimedValue();
            assertNull(tv.getValue());
            assertNull(tv.getTimestamp());

            dto.value = 42;
            push.pushUpdate(dto).getValue();
            assertEquals(42, getResourceValue());

            dto.value = null;
            push.pushUpdate(dto).getValue();
            tv = getResourceTimedValue();
            assertNull(tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());
        }

        @Test
        void testSimplePushAnnotated() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            AnnotatedDTO dto = new AnnotatedDTO();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.data = "42";
            dto.time = String.valueOf(timestamp.toEpochMilli());
            push.pushUpdate(dto).getValue();

            assertEquals(42, getResourceValue());
        }

        @Test
        void testSimplePushUpdateDuplicateDataDifferent() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            dto.duplicateDataAction = DuplicateAction.UPDATE_IF_DIFFERENT;
            push.pushUpdate(dto).getValue();

            TimedValue<Integer> tv = getResourceTimedValue();

            assertEquals(42, tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.timestamp = timestamp.plusSeconds(30);
            push.pushUpdate(dto).getValue();

            tv = getResourceTimedValue();

            assertEquals(42, tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.value = 43;
            push.pushUpdate(dto).getValue();

            tv = getResourceTimedValue();

            assertEquals(43, tv.getValue());
            assertEquals(timestamp.plusSeconds(30), tv.getTimestamp());
        }

        @Test
        void testSimplePushUpdateDuplicateDataAlways() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            dto.duplicateDataAction = DuplicateAction.UPDATE_ALWAYS;
            push.pushUpdate(dto).getValue();

            TimedValue<Integer> tv = getResourceTimedValue();

            assertEquals(42, tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.timestamp = timestamp.plusSeconds(30);
            push.pushUpdate(dto).getValue();

            tv = getResourceTimedValue();

            assertEquals(42, tv.getValue());
            assertEquals(timestamp.plusSeconds(30), tv.getTimestamp());

            dto.value = 43;
            push.pushUpdate(dto).getValue();

            tv = getResourceTimedValue();

            assertEquals(43, tv.getValue());
            assertEquals(timestamp.plusSeconds(30), tv.getTimestamp());
        }
    }

    @Nested
    class ValidMetadataPushes {

        @BeforeEach
        void setupResource() throws Exception {
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            push.pushUpdate(dto).getValue();
        }

        @Test
        void testSimplePush() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.timestamp = timestamp;
            dto.metadata = Map.of("foo", "bar");
            push.pushUpdate(dto).getValue();

            assertEquals("bar", getResourceMetadataValue());
        }

        @Test
        void testSimplePushDataAndMetadataWithNoPreexisting() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER + "_3";
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.timestamp = timestamp;
            dto.metadata = Map.of("foo", "bar");
            push.pushUpdate(dto).getValue();

            assertEquals("bar", getResourceMetadataValue(dto.provider));
        }

        @Test
        void testSimplePushUpdate() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.metadata = Map.of("foo", "bar");
            dto.timestamp = timestamp;
            push.pushUpdate(dto).getValue();

            TimedValue<Object> tv = getResourceMetadataTimedValue();
            assertEquals("bar", tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.timestamp = timestamp.plusSeconds(30);
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertEquals("bar", tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.metadata = Map.of("foo", "foobar");
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertEquals("foobar", tv.getValue());
            assertEquals(timestamp.plusSeconds(30), tv.getTimestamp());

            dto.duplicateMetadataAction = DuplicateAction.UPDATE_ALWAYS;
            dto.timestamp = timestamp.plusSeconds(60);
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertEquals("foobar", tv.getValue());
            assertEquals(timestamp.plusSeconds(60), tv.getTimestamp());
        }

        @Test
        void testAnnotatedPushUpdate() throws Exception {
            final Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

            // Create resource & provider using a push
            DefaultMetadataDto dto = new DefaultMetadataDto();
            dto.foo = "bar";
            dto.time = timestamp;
            push.pushUpdate(dto).getValue();

            TimedValue<Object> tv = getResourceMetadataTimedValue();
            assertEquals("bar", tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.time = timestamp.plusSeconds(30);
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertEquals("bar", tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.foo = "foobar";
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertEquals("foobar", tv.getValue());
            assertEquals(timestamp.plusSeconds(30), tv.getTimestamp());

            dto.foo = null;
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertEquals("foobar", tv.getValue());
            assertEquals(timestamp.plusSeconds(30), tv.getTimestamp());
        }

        @Test
        void testAnnotatedPushUpdateDuplicateAction() throws Exception {
            final Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

            // Create resource & provider using a push
            DuplicateMetadataDto dto = new DuplicateMetadataDto();
            dto.foo = "bar";
            dto.time = timestamp;
            push.pushUpdate(dto).getValue();

            TimedValue<Object> tv = getResourceMetadataTimedValue();
            assertEquals("bar", tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.time = timestamp.plusSeconds(30);
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertEquals("bar", tv.getValue());
            assertEquals(timestamp.plusSeconds(30), tv.getTimestamp());
        }

        @Test
        void testAnnotatedPushUpdateNullAction() throws Exception {
            final Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

            // Create resource & provider using a push
            NullMetadataDto dto = new NullMetadataDto();
            dto.foo = "bar";
            dto.time = timestamp;
            push.pushUpdate(dto).getValue();

            TimedValue<Object> tv = getResourceMetadataTimedValue();
            assertEquals("bar", tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());

            dto.foo = null;
            push.pushUpdate(dto).getValue();

            tv = getResourceMetadataTimedValue();
            assertNull(tv.getValue());
            assertEquals(timestamp, tv.getTimestamp());
        }
    }

    @Nested
    class FailingGenericPushes {
        @Test
        void testProvider() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = null;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
        }

        @Test
        void testService() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = null;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(RESOURCE, due.getResource());
        }

        @Test
        void testResource() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = null;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(SERVICE, due.getService());
        }

        @Test
        void testValue() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = "0xCAFEBABE";
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
        }

        @Test
        void testSimplePushWithNoPreexisting() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER + "_2";
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.type = Integer.class;
            dto.timestamp = timestamp;
            dto.metadata = Map.of("foo", "bar");
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(dto.provider, due.getProvider());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
        }

    }

    @Nested
    class FailingAnnotatedPushes {
        @Test
        void testProvider() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            AnnotatedDTO dto = new AnnotatedDTO();
            dto.provider = null;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.data = "42";
            dto.time = String.valueOf(timestamp.toEpochMilli());
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
        }

        @Test
        void testService() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            AnnotatedDTO dto = new AnnotatedDTO();
            dto.provider = PROVIDER;
            dto.service = null;
            dto.resource = RESOURCE;
            dto.data = "42";
            dto.time = String.valueOf(timestamp.toEpochMilli());
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(RESOURCE, due.getResource());
        }

        @Test
        void testResource() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            AnnotatedDTO dto = new AnnotatedDTO();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = null;
            dto.data = "42";
            dto.time = String.valueOf(timestamp.toEpochMilli());
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(SERVICE, due.getService());
        }

        @Test
        void testValue() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            AnnotatedDTO dto = new AnnotatedDTO();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.data = "Forty-two";
            dto.time = String.valueOf(timestamp.toEpochMilli());
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
        }

        @Test
        void testTimestamp() throws Exception {
            // Create resource & provider using a push
            AnnotatedDTO dto = new AnnotatedDTO();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.data = "42";
            dto.time = "Tomorrow";
            Throwable t = push.pushUpdate(dto).getFailure();

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(1, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
        }
    }

    @Nested
    class FailingBulkPushes {
        @Test
        void testMiddleFails() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;

            GenericDto dto2 = new GenericDto();
            dto2.provider = PROVIDER;
            dto2.service = SERVICE;
            dto2.resource = RESOURCE;
            dto2.value = "Forty-three";
            dto2.type = Integer.class;
            dto2.timestamp = timestamp;

            GenericDto dto3 = new GenericDto();
            dto3.provider = PROVIDER;
            dto3.service = SERVICE;
            dto3.resource = RESOURCE;
            dto3.value = "Forty-four";
            dto3.type = Integer.class;
            dto3.timestamp = timestamp;

            GenericDto dto4 = new GenericDto();
            dto4.provider = PROVIDER;
            dto4.service = SERVICE;
            dto4.resource = RESOURCE;
            dto4.value = "45";
            dto4.type = Integer.class;
            dto4.timestamp = timestamp;

            BulkGenericDto bulk = new BulkGenericDto();
            bulk.dtos = List.of(dto, dto2, dto3, dto4);

            Throwable t = push.pushUpdate(bulk).getFailure();

            assertEquals(45, getResourceValue());

            assertNotNull(t);
            assertInstanceOf(FailedUpdatesException.class, t);
            FailedUpdatesException fue = (FailedUpdatesException) t;
            List<DataUpdateException> failedUpdates = fue.getFailedUpdates();
            assertNotNull(failedUpdates);
            assertEquals(2, failedUpdates.size());
            DataUpdateException due = failedUpdates.get(0);
            assertNotNull(due);
            assertSame(dto2, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
            due = failedUpdates.get(1);
            assertNotNull(due);
            assertSame(dto3, due.getOriginalDto());
            assertEquals(PROVIDER, due.getProvider());
            assertEquals(SERVICE, due.getService());
            assertEquals(RESOURCE, due.getResource());
        }
    }


    @Nested
    class ListPushes {
        @Test
        void testEmptyList() throws Exception {
            push.pushUpdate(List.of()).getValue();
        }

        @Test
        void testOneItem() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;

            push.pushUpdate(List.of(dto)).getValue();

            assertEquals(42, getResourceValue());
        }

        @Test
        void testSeveralItems() throws Exception {
            final Instant timestamp = Instant.now();

            // Create resource & provider using a push
            GenericDto dto = new GenericDto();
            dto.provider = PROVIDER;
            dto.service = SERVICE;
            dto.resource = RESOURCE;
            dto.value = 42;
            dto.type = Integer.class;
            dto.timestamp = timestamp;

            AnnotatedDTO dto2 = new AnnotatedDTO();
            dto2.provider = PROVIDER;
            dto2.service = SERVICE;
            dto2.resource = RESOURCE;
            dto2.data = "43";
            // adds 1second to avoid out of order update on the same resource using a truncated timestamp
            dto2.time = String.valueOf(timestamp.toEpochMilli() + 1000);

            push.pushUpdate(List.of(dto, dto2)).getValue();

            assertEquals(43, getResourceValue());
        }
    }
}

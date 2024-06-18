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
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.annotation.dto.Data;
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

}

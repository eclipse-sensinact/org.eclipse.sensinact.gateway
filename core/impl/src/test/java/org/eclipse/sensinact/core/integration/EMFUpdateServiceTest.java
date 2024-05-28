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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.ModelPackageUri;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.testdata.DynamicTestSensor;
import org.eclipse.sensinact.model.core.testdata.TestAdmin;
import org.eclipse.sensinact.model.core.testdata.TestSensor;
import org.eclipse.sensinact.model.core.testdata.TestTemperatur;
import org.eclipse.sensinact.model.core.testdata.TestdataFactory;
import org.eclipse.sensinact.model.core.testdata.TestdataPackage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Tests the error behavior of the {@link DataUpdate} service with EMF based
 * provider and service objects
 */
public class EMFUpdateServiceTest {

    private static final String PROVIDER = "Provider";
    private static final String RESOURCE = "v1";
    private static final String SERVICE = "temp";
    private static final String DYNAMIC_PROVIDER = "DynamicProvider";

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

    @Nested
    public class EMFBaseDTO {

        @Test
        void basic() throws Exception {
            TestSensor sensor = TestdataFactory.eINSTANCE.createTestSensor();
            TestTemperatur temp = TestdataFactory.eINSTANCE.createTestTemperatur();
            temp.setV1("12 °C");
            sensor.setTemp(temp);
            sensor.setId(PROVIDER);

            Promise<?> update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            assertEquals("12 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));

            temp.setV1("13 °C");
            update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            assertEquals("13 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));
        }

        @Test
        void serviceOnly() throws Exception {
            TestSensor sensor = TestdataFactory.eINSTANCE.createTestSensor();
            TestTemperatur temp = TestdataFactory.eINSTANCE.createTestTemperatur();
            temp.setV1("14 °C");
            sensor.setTemp(temp);
            sensor.setId(PROVIDER);

            Promise<?> update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            assertEquals("14 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));

            TestTemperatur temp2 = TestdataFactory.eINSTANCE.createTestTemperatur();
            temp2.setV1("13 °C");

            update = push.pushUpdate(temp2);
            assertNull(update.getFailure());
            assertEquals("14 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));
        }

        @Test
        void admin() throws Exception {
            TestSensor sensor = TestdataFactory.eINSTANCE.createTestSensor();
            TestTemperatur temp = TestdataFactory.eINSTANCE.createTestTemperatur();
            TestAdmin admin = TestdataFactory.eINSTANCE.createTestAdmin();
            temp.setV1("14 °C");
            sensor.setTemp(temp);
            sensor.setId(PROVIDER);
            sensor.setAdmin(admin);
            admin.setTestAdmin("blub");
            Point p = new Point();
            admin.setLocation(p);

            Promise<?> update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            assertEquals(p, getResourceValue("TestSensor", PROVIDER, "admin", "location"));
            assertEquals("blub", getResourceValue("TestSensor", PROVIDER, "admin", "testAdmin"));
        }

    }

    @Nested
    public class EMFDynamicProvider {

        @ModelPackageUri(TestdataPackage.eNS_URI)
        @Model("DynamicTestSensor")
        @Provider(DYNAMIC_PROVIDER)
        @Service(SERVICE)
        public class TestModelDTO {
            @Resource(RESOURCE)
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Test
        void dynamicUpdateDTO() throws Exception {
            DynamicTestSensor sensor = TestdataFactory.eINSTANCE.createDynamicTestSensor();
            sensor.setId(DYNAMIC_PROVIDER);
            TestTemperatur temp = TestdataFactory.eINSTANCE.createTestTemperatur();
            temp.setV1("12 °C");

            EMap<String, org.eclipse.sensinact.model.core.provider.Service> services = sensor.getServices();
            services.put(SERVICE, temp);
            Promise<?> update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            assertEquals("12 °C", getResourceValue("DynamicTestSensor", DYNAMIC_PROVIDER, SERVICE, RESOURCE));

            TestModelDTO dto = new TestModelDTO();
            dto.data = "13 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNull(t);
            assertEquals("13 °C", getResourceValue("DynamicTestSensor", DYNAMIC_PROVIDER, SERVICE, RESOURCE));
        }

    }

    @Nested
    public class EMFEClass {

        @Provider(PROVIDER)
        public class TestEClassDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.TEST_SENSOR;

            @Service
            public EReference serviceRef = TestdataPackage.Literals.TEST_SENSOR__TEMP;

            @Resource(RESOURCE)
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Provider(PROVIDER)
        public class TestEClassServiceDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.TEST_SENSOR;

            @Resource(RESOURCE)
            @Data
            @Service("temp")
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Provider(DYNAMIC_PROVIDER)
        public class DynamicTestEClassDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.DYNAMIC_TEST_SENSOR;

            @Service
            public EClass service = TestdataPackage.Literals.TEST_TEMPERATUR;

            @Resource(RESOURCE)
            @Service("tmp")
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Provider(PROVIDER)
        public class NonDynamicTestEClassDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.TEST_SENSOR;

            @Service
            public EClass service = TestdataPackage.Literals.TEST_TEMPERATUR;

            @Resource(RESOURCE)
            @Service("tmp")
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Provider(PROVIDER)
        public class NonDynamicTestDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.TEST_SENSOR;

            @Service
            public EClass service = TestdataPackage.Literals.TEST_TEMPERATUR;

            @Resource(RESOURCE)
            @Service("tmp")
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Provider(PROVIDER)
        @ModelPackageUri(TestdataPackage.eNS_URI)
        @Model("TestSensor")
        public class NonDynamicTestNoEClassDTO {

            @Resource(RESOURCE)
            @Service("tmp")
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Test
        void updateDTOERef() throws Exception {
            TestEClassDTO dto = new TestEClassDTO();
            dto.data = "13 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNull(t);
            assertEquals("13 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));
        }

        @Test
        void updateDTOEClass() throws Exception {
            TestEClassServiceDTO dto = new TestEClassServiceDTO();
            dto.data = "13 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNull(t);
            assertEquals("13 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));
        }

        @Test
        void dynamicUpdateDTOEClass() throws Exception {
            DynamicTestEClassDTO dto = new DynamicTestEClassDTO();
            dto.data = "13 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNull(t);
            assertEquals("13 °C", getResourceValue("DynamicTestSensor", DYNAMIC_PROVIDER, "tmp", RESOURCE));
        }

        @Test
        void nonDynamicUpdateDTOEClass() throws Exception {
            NonDynamicTestEClassDTO dto = new NonDynamicTestEClassDTO();
            dto.data = "13 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNotNull(t);
        }

        @Test
        void nonDynamicUpdateDTONoEClass() throws Exception {
            NonDynamicTestNoEClassDTO dto = new NonDynamicTestNoEClassDTO();
            dto.data = "13 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNotNull(t);
        }
    }

    private Object getResourceValue(String model, String provider, String service, String resource)
            throws InvocationTargetException, InterruptedException {
        Object value = gt
                .execute(new ResourceCommand<Object>(TestdataPackage.eNS_URI, model, provider, service, resource) {

                    @Override
                    protected Promise<Object> call(SensinactResource resource, PromiseFactory pf) {
                        return resource.getValue().map(t -> t.getValue());
                    }
                }).getValue();
        return value;
    }
}

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.ModelPackageUri;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.provider.MetadataValue;
import org.eclipse.sensinact.model.core.provider.ProviderFactory;
import org.eclipse.sensinact.model.core.provider.ResourceValueMetadata;
import org.eclipse.sensinact.model.core.testdata.DynamicTestSensor;
import org.eclipse.sensinact.model.core.testdata.TestAdmin;
import org.eclipse.sensinact.model.core.testdata.TestResource;
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
        void serviceWithMetadataWithTimestamp() throws Exception {
            TestSensor sensor = TestdataFactory.eINSTANCE.createTestSensor();
            TestTemperatur temp = TestdataFactory.eINSTANCE.createTestTemperatur();
            temp.setV1("14 °C");
            sensor.setTemp(temp);
            sensor.setId(PROVIDER);

            Instant oneMinuteAgo = Instant.now().minusSeconds(60);
            EMap<ETypedElement, ResourceValueMetadata> metadata = temp.getMetadata();
            ResourceValueMetadata md = ProviderFactory.eINSTANCE.createResourceValueMetadata();
            md.setTimestamp(oneMinuteAgo);
            metadata.put(TestdataPackage.eINSTANCE.getTestTemperatur_V1(), md);

            Promise<?> update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            assertEquals("14 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));
            Instant current = getResourceTimestamp("TestSensor", PROVIDER, SERVICE, RESOURCE);
            assertNotNull(current);
            assertEquals(oneMinuteAgo, current);
        }

        @Test
        void serviceWithMetadataWithoutTimestamp() throws Exception {
            TestSensor sensor = TestdataFactory.eINSTANCE.createTestSensor();
            TestTemperatur temp = TestdataFactory.eINSTANCE.createTestTemperatur();
            temp.setV1("14 °C");
            sensor.setTemp(temp);
            sensor.setId(PROVIDER);

            EMap<ETypedElement, ResourceValueMetadata> metadata = temp.getMetadata();
            ResourceValueMetadata md = ProviderFactory.eINSTANCE.createResourceValueMetadata();
            metadata.put(TestdataPackage.eINSTANCE.getTestTemperatur_V1(), md);
            EMap<String, MetadataValue> extra = md.getExtra();
            MetadataValue fcmd = ProviderFactory.eINSTANCE.createMetadataValue();
            extra.put("FCMD Name", fcmd);

            Instant before = Instant.now();
            Promise<?> update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            Instant after = Instant.now();
            assertEquals("14 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));
            Instant current = getResourceTimestamp("TestSensor", PROVIDER, SERVICE, RESOURCE);
            assertNotNull(current);
            assertFalse(before.isAfter(current));
            assertFalse(after.isBefore(current));

            temp.setV1("13 °C");
            before = Instant.now();
            update = push.pushUpdate(sensor);
            Throwable failure = update.getFailure();
            assertNull(failure, () -> "Fails with " + failure.getMessage());
            assertEquals("13 °C", getResourceValue("TestSensor", PROVIDER, SERVICE, RESOURCE));
            current = getResourceTimestamp("TestSensor", PROVIDER, SERVICE, RESOURCE);
            assertNotNull(current);
            after = Instant.now();
            assertFalse(before.isAfter(current));
            assertFalse(after.isBefore(current));
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
            Point p = new Point(Coordinates.EMPTY, null, null);
            admin.setLocation(p);
            admin.setDescription("Foobar");

            Promise<?> update = push.pushUpdate(sensor);
            assertNull(update.getFailure());
            assertEquals(p, getResourceValue("TestSensor", PROVIDER, "admin", "location"));
            assertEquals("Foobar", getResourceValue("TestSensor", PROVIDER, "admin", "description"));
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

            @ServiceModel
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

            @ServiceModel
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

            @ServiceModel
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

        @Provider(PROVIDER)
        public class NonDynamicComplexObjectDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.COMPLEX_TEST_SENSOR;

            @Service
            public EReference service = TestdataPackage.Literals.COMPLEX_TEST_SENSOR__TEMP;

            @Resource("testResource")
            @Data
            public TestResource data;

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

        @Test
        void complexResource() throws Exception {
            {
                NonDynamicComplexObjectDTO dto = new NonDynamicComplexObjectDTO();
                TestResource resource = TestdataFactory.eINSTANCE.createTestResource();
                resource.setFoo("test1");
                resource.setBar("test2");
                dto.data = resource;
                dto.timestamp = Instant.now().toEpochMilli();

                Promise<?> update = push.pushUpdate(dto);
                Throwable t = update.getFailure();
                assertNull(t);
                Object o = getResourceValue(TestdataPackage.Literals.COMPLEX_TEST_SENSOR.getName(), PROVIDER,
                        TestdataPackage.Literals.COMPLEX_TEST_SENSOR__TEMP.getName(),
                        TestdataPackage.Literals.TEST_TEMPERATUR_WITH_COMPLEX__TEST_RESOURCE.getName());
                assertNotNull(o);
                assertInstanceOf(TestResource.class, o);
                TestResource result = (TestResource) o;
                assertEquals("test1", result.getFoo());
                assertEquals("test2", result.getBar());
            }

            {
                NonDynamicComplexObjectDTO dto = new NonDynamicComplexObjectDTO();
                TestResource resource = TestdataFactory.eINSTANCE.createTestResource();
                resource.setFoo("test3");
                resource.setBar("test4");
                dto.data = resource;
                dto.timestamp = Instant.now().toEpochMilli();
                Promise<?> update = push.pushUpdate(dto);
                Throwable t = update.getFailure();
                assertNull(t);
                Object o = getResourceValue(TestdataPackage.Literals.COMPLEX_TEST_SENSOR.getName(), PROVIDER,
                        TestdataPackage.Literals.COMPLEX_TEST_SENSOR__TEMP.getName(),
                        TestdataPackage.Literals.TEST_TEMPERATUR_WITH_COMPLEX__TEST_RESOURCE.getName());
                assertNotNull(o);
                assertInstanceOf(TestResource.class, o);
                TestResource result = (TestResource) o;
                assertEquals("test3", result.getFoo());
                assertEquals("test4", result.getBar());
            }
        }
    }

    @Nested
    public class EMFStringServiceModel {

        @Provider(DYNAMIC_PROVIDER)
        public class DynamicTestStringServiceModelDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.DYNAMIC_TEST_SENSOR;

            @ServiceModel
            public String serviceModel = "TestTemperatur";

            @Resource(RESOURCE)
            @Service("temperature")
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Provider(DYNAMIC_PROVIDER)
        public class DynamicTestStringServiceModelWithMetadataDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.DYNAMIC_TEST_SENSOR;

            @ServiceModel
            public String serviceModel = "TestTemperatur";

            @Service
            public String serviceName = "humidity";

            @Resource(RESOURCE)
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Provider(PROVIDER)
        public class NonDynamicTestStringServiceModelDTO {

            @Model
            public EClass providerEClass = TestdataPackage.Literals.TEST_SENSOR;

            @ServiceModel
            public String serviceModel = "TestTemperatur";

            @Resource(RESOURCE)
            @Service("tmp")
            @Data
            public String data;

            @Timestamp(ChronoUnit.MILLIS)
            public long timestamp;
        }

        @Test
        void dynamicUpdateDTOStringServiceModel() throws Exception {
            DynamicTestStringServiceModelDTO dto = new DynamicTestStringServiceModelDTO();
            dto.data = "15 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNull(t, () -> "Failed with: " + (t != null ? t.getMessage() : "null"));
            assertEquals("15 °C", getResourceValue("DynamicTestSensor", DYNAMIC_PROVIDER, "temperature", RESOURCE));
        }

        @Test
        void dynamicUpdateDTOStringServiceModelMultipleUpdates() throws Exception {
            // First update
            DynamicTestStringServiceModelDTO dto = new DynamicTestStringServiceModelDTO();
            dto.data = "16 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t1 = update.getFailure();
            assertNull(t1, () -> "First update failed with: " + (t1 != null ? t1.getMessage() : "null"));
            assertEquals("16 °C", getResourceValue("DynamicTestSensor", DYNAMIC_PROVIDER, "temperature", RESOURCE));

            // Second update
            dto.data = "17 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            update = push.pushUpdate(dto);
            Throwable t2 = update.getFailure();
            assertNull(t2, () -> "Second update failed with: " + (t2 != null ? t2.getMessage() : "null"));
            assertEquals("17 °C", getResourceValue("DynamicTestSensor", DYNAMIC_PROVIDER, "temperature", RESOURCE));
        }

        @Test
        void dynamicUpdateDTOStringServiceModelWithServiceName() throws Exception {
            DynamicTestStringServiceModelWithMetadataDTO dto = new DynamicTestStringServiceModelWithMetadataDTO();
            dto.data = "18 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNull(t, () -> "Failed with: " + (t != null ? t.getMessage() : "null"));
            assertEquals("18 °C", getResourceValue("DynamicTestSensor", DYNAMIC_PROVIDER, "humidity", RESOURCE));
        }

        @Test
        void nonDynamicUpdateDTOStringServiceModel() throws Exception {
            // This should fail because TestSensor is not a dynamic provider
            NonDynamicTestStringServiceModelDTO dto = new NonDynamicTestStringServiceModelDTO();
            dto.data = "19 °C";
            dto.timestamp = Instant.now().toEpochMilli();

            Promise<?> update = push.pushUpdate(dto);
            Throwable t = update.getFailure();
            assertNotNull(t, "Should fail for non-dynamic provider with String service model");
        }
    }

    private Object getResourceValue(String model, String provider, String service, String resource)
            throws InvocationTargetException, InterruptedException {
        return gt.execute(new ResourceCommand<Object>(TestdataPackage.eNS_URI, model, provider, service, resource) {

            @Override
            protected Promise<Object> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getValue().map(t -> t.getValue());
            }
        }).getValue();
    }

    private Instant getResourceTimestamp(String model, String provider, String service, String resource)
            throws InvocationTargetException, InterruptedException {
        return gt.execute(new ResourceCommand<Instant>(TestdataPackage.eNS_URI, model, provider, service, resource) {

            @Override
            protected Promise<Instant> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getValue().map(t -> t.getTimestamp());
            }
        }).getValue();
    }
}

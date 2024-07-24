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
*   Data In Motion - initial API and implementation
*   Kentyou - fixes and updates to start basic testing
**********************************************************************/
package org.eclipse.sensinact.core.twin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.URIMappingRegistryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sensinact.core.command.impl.ActionHandler;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 *
 * @author Juergen Albert
 * @since 10 Oct 2022
 */
@ExtendWith(MockitoExtension.class)
public class SensinactTwinTest {

    private static final String TEST_MODEL = "testmodel";
    private static final String TEST_MODEL_WITH_METADATA = "testmodel_Metadata";
    private static final String TEST_PROVIDER = "testprovider";
    private static final String TEST_SERVICE = "testservice";
    private static final String TEST_RESOURCE = "testValue";
    private static final String TEST_ACTION_RESOURCE = "testAction";

    @Mock
    NotificationAccumulator accumulator;

    @Mock
    ActionHandler actionHandler;

    private PromiseFactory promiseFactory = new PromiseFactory(PromiseFactory.inlineExecutor());

    private ResourceSet resourceSet;

    private ModelNexus nexus;

    private SensinactModelManagerImpl manager;

    private SensinactDigitalTwinImpl twinImpl;

    private EPackage ePackage;

    @BeforeEach
    void start() throws IOException {
        resourceSet = EMFTestUtil.createResourceSet();
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator, actionHandler);
        manager = new SensinactModelManagerImpl(nexus);
        twinImpl = new SensinactDigitalTwinImpl(nexus, promiseFactory);

        manager.createModel(TEST_MODEL).withService(TEST_SERVICE).withResource(TEST_RESOURCE).withType(Integer.class)
                .build().withResource(TEST_ACTION_RESOURCE).withType(Double.class)
                .withAction(List.of(new SimpleEntry<>("foo", String.class), new SimpleEntry<>("bar", Instant.class)))
                .buildAll();

        manager.createModel(TEST_MODEL_WITH_METADATA).withService(TEST_SERVICE)
                    .withResource(TEST_RESOURCE).withType(Integer.class).withDefaultMetadata(Map.of("foo", "bar")).build()
                    .withResource(TEST_ACTION_RESOURCE).withType(Double.class)
                        .withAction(List.of(new SimpleEntry<>("foo", String.class), new SimpleEntry<>("bar", Instant.class)))
                        .withDefaultMetadata(Map.of("fizz", "buzz"))
                .buildAll();

        URI ProviderPackageURI = URI.createURI(ProviderPackage.eNS_URI);

        URIMappingRegistryImpl.INSTANCE.put(
                URI.createURI("https://eclipse.org/../../../models/src/main/resources/model/sensinact.ecore"),
                ProviderPackageURI);

        XMLResource.URIHandler handler = new XMLResource.URIHandler() {

            @Override
            public URI deresolve(URI arg0) {
                return arg0;
            }

            @Override
            public URI resolve(URI arg0) {
                if (arg0.lastSegment().equals("sensinact.ecore")) {
                    return ProviderPackageURI.appendFragment(arg0.fragment());
                }
                return arg0;
            }

            @Override
            public void setBaseURI(URI arg0) {
                // TODO Auto-generated method stub
            }
        };

        Resource extendedPackageResource = resourceSet
                .createResource(URI.createURI("https://eclipse.org/sensinact/test/1.0"));
        InputStream ín = getClass().getResourceAsStream("/model/extended.ecore");

        assertNotNull(ín);

        extendedPackageResource.load(ín, Collections.singletonMap(XMLResource.OPTION_URI_HANDLER, handler));

        ePackage = (EPackage) extendedPackageResource.getContents().get(0);

        assertNotNull(ePackage);

    }

    @Nested
    public class ProviderTests {

        @Test
        void testEmptyModelNexus() {
            assertEquals(List.of("sensiNact"),
                    twinImpl.getProviders().stream().map(SensinactProvider::getName).collect(Collectors.toList()));
        }

        @Test
        void testCreateProvider() {
            SensinactProvider provider = twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);

            assertEquals(TEST_MODEL, provider.getModelName());
            assertEquals(TEST_PROVIDER, provider.getName());
            assertEquals(Set.of(TEST_SERVICE, "admin"), provider.getServices().keySet());
            assertEquals(Set.of(TEST_RESOURCE, TEST_ACTION_RESOURCE),
                    provider.getServices().get(TEST_SERVICE).getResources().keySet());
        }

        @Test
        void testMetadataWithoutValue() throws Exception {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            SensinactResourceImpl rc = twinImpl.getResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE);
            rc.setMetadataValue("description", "test", Instant.now()).getValue();
            assertEquals("test", rc.getMetadataValue("description").getValue().getValue());
        }

        @Test
        void testCreateProviderDefaultMetadata() throws InvocationTargetException, InterruptedException {
            SensinactProvider provider = twinImpl.createProvider(TEST_MODEL_WITH_METADATA, TEST_PROVIDER);

            SensinactService service = provider.getServices().get(TEST_SERVICE);
            SensinactResource resource = service.getResources().get(TEST_RESOURCE);

            Map<String, Object> metadata = resource.getMetadataValues().getValue();
            assertEquals(Set.of("foo", "timestamp"), metadata.keySet());
            assertEquals("bar", metadata.get("foo"));
            assertNull(metadata.get("timestamp"));

            TimedValue<Object> metadataValue = resource.getMetadataValue("foo").getValue();

            assertEquals("bar", metadataValue.getValue());
            assertNull(metadataValue.getTimestamp());

            resource = service.getResources().get(TEST_ACTION_RESOURCE);

            metadata = resource.getMetadataValues().getValue();
            assertEquals(Set.of("fizz", "timestamp"), metadata.keySet());
            assertEquals("buzz", metadata.get("fizz"));
            assertNull(metadata.get("timestamp"));

            metadataValue = resource.getMetadataValue("fizz").getValue();

            assertEquals("buzz", metadataValue.getValue());
            assertNull(metadataValue.getTimestamp());
        }

        @Test
        void testOverrideProviderDefaultMetadata() throws InvocationTargetException, InterruptedException {
            SensinactProvider provider = twinImpl.createProvider(TEST_MODEL_WITH_METADATA, TEST_PROVIDER);

            SensinactService service = provider.getServices().get(TEST_SERVICE);
            SensinactResource resource = service.getResources().get(TEST_RESOURCE);

            Instant timestamp = Instant.parse("2020-01-01T00:00:00Z");
            resource.setMetadataValue("extra", 42, timestamp).getValue();

            Map<String, Object> metadata = resource.getMetadataValues().getValue();
            assertEquals(Set.of("foo", "timestamp", "extra"), metadata.keySet());
            assertEquals("bar", metadata.get("foo"));
            assertEquals(42, metadata.get("extra"));
            assertNull(metadata.get("timestamp"));

            TimedValue<Object> metadataValue = resource.getMetadataValue("foo").getValue();

            assertEquals("bar", metadataValue.getValue());
            assertNull(metadataValue.getTimestamp());

            metadataValue = resource.getMetadataValue("extra").getValue();

            assertEquals(42, metadataValue.getValue());
            assertEquals(timestamp, metadataValue.getTimestamp());

            resource = service.getResources().get(TEST_ACTION_RESOURCE);
            resource.setMetadataValue("fizz", 42, timestamp).getValue();

            metadata = resource.getMetadataValues().getValue();
            assertEquals(Set.of("fizz", "timestamp"), metadata.keySet());
            assertEquals(42, metadata.get("fizz"));
            assertNull(metadata.get("timestamp"));

            metadataValue = resource.getMetadataValue("fizz").getValue();

            assertEquals(42, metadataValue.getValue());
            assertEquals(timestamp, metadataValue.getTimestamp());
        }

        @Test
        void basicResourceSet() throws Exception {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            SensinactResourceImpl resource = twinImpl.getResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE);

            assertEquals(Integer.class, resource.getType());
            assertEquals(ResourceType.SENSOR, resource.getResourceType());
            Promise<TimedValue<?>> value = resource.getValue();
            assertEquals(null, value.getValue().getValue());
            assertEquals(null, value.getValue().getTimestamp());

            Instant timestamp = Instant.parse("2023-02-14T15:00:00.000Z");
            resource.setValue(42, timestamp);
            value = resource.getValue();
            assertEquals(42, value.getValue().getValue());
            assertEquals(timestamp, value.getValue().getTimestamp());
        }

        @Test
        void basicActionResource() throws Exception {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            SensinactResourceImpl resource = twinImpl.getResource(TEST_PROVIDER, TEST_SERVICE, TEST_ACTION_RESOURCE);

            assertEquals(Double.class, resource.getType());
            assertEquals(ResourceType.ACTION, resource.getResourceType());
            Promise<TimedValue<?>> value = resource.getValue();
            assertEquals(IllegalArgumentException.class, value.getFailure().getClass());

            assertEquals(List.of(new SimpleEntry<>("foo", String.class), new SimpleEntry<>("bar", Instant.class)),
                    resource.getArguments());

            Map<String, Object> arguments = Map.of("foo", "bar", "foobar", Instant.now());

            Mockito.when(actionHandler.act(EMFUtil.constructPackageUri(TEST_MODEL), TEST_MODEL, TEST_PROVIDER, TEST_SERVICE, TEST_ACTION_RESOURCE, arguments))
                    .thenReturn(promiseFactory.<Object>resolved(4.2D).delay(1000));

            Promise<Object> act = resource.act(arguments);

            assertFalse(act.isDone());
            assertEquals(4.2D, act.getValue());
        }
    }

    @Nested
    public class FilterTests {

        @Test
        void simpleEmptyFilter() {
            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, null, null, null);
            assertEquals(1, list.size());

            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            list = twinImpl.filteredSnapshot(null, null, null, null);
            assertEquals(2, list.size());
        }

        @Test
        void simpleProviderNameFilter() {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);

            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, p -> "foo".equals(p.getName()), null, null);
            assertTrue(list.isEmpty());

            list = twinImpl.filteredSnapshot(null, p -> TEST_PROVIDER.equals(p.getName()), null, null);
            assertEquals(1, list.size());
        }

        @Test
        void testSnapshotProviderDefaultMetadata() throws InvocationTargetException, InterruptedException {
            twinImpl.createProvider(TEST_MODEL_WITH_METADATA, TEST_PROVIDER);

            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, p -> TEST_PROVIDER.equals(p.getName()), null, null);
            assertEquals(1, list.size());

            ResourceSnapshot rs = list.get(0).getServices().stream()
                .filter(s -> TEST_SERVICE.equals(s.getName()))
                .flatMap(s -> s.getResources().stream())
                .filter(r -> TEST_RESOURCE.equals(r.getName()))
                .findFirst().get();

            Map<String, Object> metadata = rs.getMetadata();
            assertEquals(Set.of("foo", "timestamp"), metadata.keySet());
            assertEquals("bar", metadata.get("foo"));
            assertNull(metadata.get("timestamp"));

            rs = list.get(0).getServices().stream()
                    .filter(s -> TEST_SERVICE.equals(s.getName()))
                    .flatMap(s -> s.getResources().stream())
                    .filter(r -> TEST_ACTION_RESOURCE.equals(r.getName()))
                    .findFirst().get();

            metadata = rs.getMetadata();
            assertEquals(Set.of("fizz", "timestamp"), metadata.keySet());
            assertEquals("buzz", metadata.get("fizz"));
            assertNull(metadata.get("timestamp"));
        }

        @Test
        void simpleResourceValueFilter() throws Exception {
            twinImpl.createProvider(TEST_MODEL, TEST_PROVIDER);
            twinImpl.getResource(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE).setValue(5).getValue();

            Predicate<ResourceSnapshot> p = r -> TEST_RESOURCE.equals(r.getName());

            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, null, null, p);
            assertEquals(1, list.size());
            assertEquals(2, list.get(0).getServices().size());
            ServiceSnapshot serviceSnapshot = list.get(0).getServices().stream()
                    .filter(s -> TEST_SERVICE.equals(s.getName())).findFirst().get();
            assertEquals(5, serviceSnapshot.getResources().get(0).getValue().getValue());
        }

        @Test
        void simpleResourceValueFilterWithDynamicResource() throws Exception {
            DynamicProvider provider = (DynamicProvider) EcoreUtil
                    .create((EClass) ePackage.getEClassifier("DynamicTemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.getServices().put(TEST_SERVICE, testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("foo"), "fizz");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            nexus.save(provider);

            Predicate<ResourceSnapshot> p = r -> "foo".equals(r.getName());

            List<ProviderSnapshot> list = twinImpl.filteredSnapshot(null, null, null, p);
            assertEquals(1, list.size());
            assertEquals(4, list.get(0).getServices().size());
            ServiceSnapshot serviceSnapshot = list.get(0).getServices().stream()
                    .filter(s -> TEST_SERVICE.equals(s.getName())).findFirst().get();
            assertEquals("fizz", serviceSnapshot.getResources().get(0).getValue().getValue());
        }
    }
}

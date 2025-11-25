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
package org.eclipse.sensinact.core.model.nexus;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.URIMappingRegistryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Juergen Albert
 * @since 10 Oct 2022
 */
@ExtendWith(MockitoExtension.class)
public class NexusTest {

    @Mock
    NotificationAccumulator accumulator;

    private ResourceSet resourceSet;

    @BeforeEach
    void start() {
        resourceSet = EMFTestUtil.createResourceSet();
    }

    @AfterEach
    void tearDown() throws IOException {
        Path data = Paths.get("data");
        if (Files.exists(data)) {
            Files.walkFileTree(data, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        throw exc;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) { // directory iteration failed
                        throw exc;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Nested
    public class BasicModelTests {

        /** TEST_VALUE */
        private static final String TEST_VALUE = "testValue";
        /** TESTSERVICE */
        private static final String TESTSERVICE = "testservice";
        /** TESTPROVIDER */
        private static final String TESTPROVIDER = "testprovider";
        /** TEST_MODEL */
        private static final String TEST_MODEL = "TestModel";
        private final String TEST_PKG = EMFUtil.constructPackageUri(TEST_MODEL);

        @Test
        void basicTest() {

            resourceSet.setURIConverter(new URIConverter() {

                @Override
                public void setAttributes(URI uri, Map<String, ?> attributes, Map<?, ?> options) throws IOException {
                    // TODO Auto-generated method stub

                }

                @Override
                public URI normalize(URI uri) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public Map<URI, URI> getURIMap() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public EList<URIHandler> getURIHandlers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public URIHandler getURIHandler(URI uri) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public EList<ContentHandler> getContentHandlers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public Map<String, ?> getAttributes(URI uri, Map<?, ?> options) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public boolean exists(URI uri, Map<?, ?> options) {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void delete(URI uri, Map<?, ?> options) throws IOException {
                    // TODO Auto-generated method stub

                }

                @Override
                public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public OutputStream createOutputStream(URI uri) throws IOException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public InputStream createInputStream(URI uri) throws IOException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public Map<String, ?> contentDescription(URI uri, Map<?, ?> options) throws IOException {
                    // TODO Auto-generated method stub
                    return null;
                }
            });

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            EClass model = nexus.createModel(TEST_MODEL, now);
            EReference service = nexus.createService(model, TESTSERVICE, TESTSERVICE, now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
                    null);
            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);

            Provider provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            assertNotNull(provider);

            assertNotNull(provider.getAdmin());
            assertEquals(TESTPROVIDER, provider.getAdmin().getFriendlyName());

            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature(TESTSERVICE);
            assertNotNull(serviceFeature);
            assertEquals(TESTSERVICE, serviceFeature.getName());
            EClass eClass = (EClass) serviceFeature.getEType();
            assertEquals("Testservice", eClass.getName());

            Service svc = (Service) provider.eGet(serviceFeature);

            assertNotNull(svc);

            EStructuralFeature valueFeature = eClass.getEStructuralFeature(TEST_VALUE);

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object value = svc.eGet(valueFeature);
            assertEquals("test", value);
        }

        @Test
        void sensiNactProvider() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Collection<Provider> providers = nexus.getProviders();

            assertEquals(1, providers.size());
            Provider provider = providers.iterator().next();

            assertNotNull(provider.getAdmin());
            assertEquals("sensiNact", provider.getAdmin().getFriendlyName());

            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("system");
            assertNotNull(serviceFeature);
            assertEquals("system", serviceFeature.getName());
            EClass eClass = (EClass) serviceFeature.getEType();

            Service service = (Service) provider.eGet(serviceFeature);

            assertNotNull(service);

            EStructuralFeature versionFeature = eClass.getEStructuralFeature("version");

            assertNotNull(versionFeature);
            assertEquals(EcorePackage.Literals.EDOUBLE, versionFeature.getEType());

            Object value = service.eGet(versionFeature);
            assertEquals(0.1D, value);

            EStructuralFeature startedFeature = eClass.getEStructuralFeature("started");

            assertNotNull(startedFeature);
            assertEquals(startedFeature.getEType(), ProviderPackage.eINSTANCE.getEInstant());

            Instant started = (Instant) service.eGet(startedFeature);
            assertNotNull(started);
            assertEquals(Instant.now().truncatedTo(DAYS), started.truncatedTo(DAYS));
        }

        @Test
        void basicServiceExtensionTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            EClass model = nexus.createModel(TEST_MODEL, now);
            EReference service = nexus.createService(model, TESTSERVICE, TESTSERVICE, now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
                    null);
            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);

            Provider provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            assertNotNull(provider);
            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature(TESTSERVICE);
            Service svc = (Service) provider.eGet(serviceFeature);

            EStructuralFeature valueFeature = svc.eClass().getEStructuralFeature(TEST_VALUE);

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object value = svc.eGet(valueFeature);
            assertEquals("test", value);

            EAttribute resource2 = nexus.createResource(service.getEReferenceType(), "testValue2", String.class, now,
                    null);
            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource2, "test",
                    Instant.now());

            Provider updatedProvider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            assertEquals(provider, updatedProvider);

            Service updateService = (Service) updatedProvider.eGet(serviceFeature);
            assertEquals(svc, updateService);

            value = updateService.eGet(valueFeature);
            assertEquals("test", value);

            EStructuralFeature valueFeature2 = updateService.eClass().getEStructuralFeature("testValue2");

            assertNotNull(valueFeature2);
            assertEquals(valueFeature2.getEType(), EcorePackage.Literals.ESTRING);

            Object value2 = updateService.eGet(valueFeature2);
            assertEquals("test", value2);
        }

        @Test
        void basicSecondServiceTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            EClass model = nexus.createModel(TEST_MODEL, now);
            EReference service = nexus.createService(model, TESTSERVICE, TESTSERVICE, now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
                    null);
            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);

            Provider provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            assertNotNull(provider);

            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature(TESTSERVICE);
            Service svc = (Service) provider.eGet(serviceFeature);

            EStructuralFeature valueFeature = svc.eClass().getEStructuralFeature(TEST_VALUE);

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object value = svc.eGet(valueFeature);
            assertEquals("test", value);

            EReference service2 = nexus.createService(model, "testservice2", "testservice2", now);
            EAttribute resource2 = nexus.createResource(service2.getEReferenceType(), TEST_VALUE, String.class, now,
                    null);
            nexus.handleDataUpdate(p, service2.getName(), service2, service2.getEReferenceType(), resource2, "test2",
                    Instant.now());

            svc = (Service) p.eGet(serviceFeature);
            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            value = svc.eGet(valueFeature);
            assertEquals("test", value);

            // now check the new feature

            EStructuralFeature serviceFeature2 = p.eClass().getEStructuralFeature("testservice2");
            Service svc2 = (Service) p.eGet(serviceFeature2);

            EStructuralFeature valueFeature2 = svc2.eClass().getEStructuralFeature(TEST_VALUE);

            assertNotNull(valueFeature2);
            assertEquals(valueFeature2.getEType(), EcorePackage.Literals.ESTRING);

            Object value2 = svc2.eGet(valueFeature2);
            assertEquals("test2", value2);
        }

        @Test
        void basicServiceWithModelNameTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            EClass model = nexus.createModel(TEST_MODEL, now);
            EReference service = nexus.createService(model, TESTSERVICE, "MyService", now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
                    null);

            assertEquals("MyService", service.getEReferenceType().getName());

            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);

        }

        @Test
        void basicTwoServicesWithSameModelNameTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();
            EClass model = nexus.createModel(TEST_MODEL, now);
            EReference service = nexus.createService(model, TESTSERVICE, "MyService", now);
            EReference service2 = nexus.createService(model, "testservice2", "MyService", now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
                    null);

            assertEquals("MyService", service.getEReferenceType().getName());
            assertEquals("MyService", service2.getEReferenceType().getName());
            assertEquals(service2.getEReferenceType(), service2.getEReferenceType());

            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);

        }

        @Test
        void basicFullProviderTest() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            for (int modelIdx = 0; modelIdx < 2; modelIdx++) {
                EClass model = nexus.createModel("model_" + modelIdx, Instant.now());
                for (int svcIdx = 0; svcIdx < 2; svcIdx++) {
                    EReference service = nexus.createService(model, "service_" + svcIdx, "service_" + svcIdx,
                            Instant.now());
                    EAttribute resource = nexus.createResource(service.getEReferenceType(), "resource", Integer.class,
                            Instant.now(), null);
                    Provider p;
                    if (svcIdx == 0) {
                        p = nexus.createProviderInstance(EMFUtil.constructPackageUri(model),
                                EMFUtil.getModelName(model), "provider_" + modelIdx, Instant.now());
                    } else {
                        p = nexus.getProvider("provider_" + modelIdx);
                    }

                    System.out.println("Calling update with model_" + modelIdx + ", provider_" + modelIdx + ", service_"
                            + svcIdx + ", resource , 42...");
                    nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, 42,
                            Instant.now());
                }
            }

            for (int modelIdx = 0; modelIdx < 2; modelIdx++) {
                Provider provider = nexus.getProvider(null, "model_" + modelIdx, "provider_" + modelIdx);
                assertNotNull(provider);

                for (int svcIdx = 0; svcIdx < 2; svcIdx++) {
                    EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("service_" + svcIdx);
                    Service service = (Service) provider.eGet(serviceFeature);
                    EStructuralFeature valueFeature = service.eClass().getEStructuralFeature("resource");
                    assertNotNull(valueFeature);
                    assertEquals(valueFeature.getEType(), EcorePackage.Literals.EINTEGER_OBJECT);
                    Object value = service.eGet(valueFeature);
                    assertEquals(42, value);
                }
            }
        }

        @Test
        void basicFullProviderTestWithPackage() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            for (int modelIdx = 0; modelIdx < 2; modelIdx++) {
                EClass model = nexus.createModel(TEST_PKG, "model_" + modelIdx, Instant.now());
                for (int svcIdx = 0; svcIdx < 2; svcIdx++) {
                    EReference service = nexus.createService(model, "service_" + svcIdx, "service_" + svcIdx,
                            Instant.now());
                    EAttribute resource = nexus.createResource(service.getEReferenceType(), "resource", Integer.class,
                            Instant.now(), null);
                    Provider p;
                    if (svcIdx == 0) {
                        p = nexus.createProviderInstance(TEST_PKG, EMFUtil.getModelName(model), "provider_" + modelIdx,
                                Instant.now());
                    } else {
                        p = nexus.getProvider("provider_" + modelIdx);
                    }

                    System.out.println("Calling update with model_" + modelIdx + ", provider_" + modelIdx + ", service_"
                            + svcIdx + ", resource , 42...");
                    nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, 42,
                            Instant.now());
                }
            }

            for (int modelIdx = 0; modelIdx < 2; modelIdx++) {
                Provider provider = nexus.getProvider(TEST_PKG, "model_" + modelIdx, "provider_" + modelIdx);
                assertNotNull(provider);

                for (int svcIdx = 0; svcIdx < 2; svcIdx++) {
                    EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("service_" + svcIdx);
                    Service service = (Service) provider.eGet(serviceFeature);
                    EStructuralFeature valueFeature = service.eClass().getEStructuralFeature("resource");
                    assertNotNull(valueFeature);
                    assertEquals(valueFeature.getEType(), EcorePackage.Literals.EINTEGER_OBJECT);
                    Object value = service.eGet(valueFeature);
                    assertEquals(42, value);
                }
            }
        }

//		@Test
//        void basicPersistanceTest() throws IOException {
//
//            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
//            Instant now = Instant.now();
//
//            EClass model = nexus.createModel(TEST_MODEL, now);
//            EReference service = nexus.createService(model, TESTSERVICE, now);
//            EReference service2 = nexus.createService(model, "testservice2", now);
//            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
//                    null);
//            EAttribute resource2 = nexus.createResource(service2.getEReferenceType(), TEST_VALUE, String.class, now,
//                    null);
//            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);
//
//            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);
//            nexus.handleDataUpdate(p, service2.getName(), service2, service2.getEReferenceType(), resource2, "test2",
//                    now);
//
//            nexus.shutDown();
//
//            nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
//            Provider provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
//            assertNotNull(provider);
//            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("testservice2");
//            Service svc = (Service) provider.eGet(serviceFeature);
//
//            EStructuralFeature valueFeature = svc.eClass().getEStructuralFeature(TEST_VALUE);
//
//            assertNotNull(valueFeature);
//            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);
//
//            Object value = svc.eGet(valueFeature);
//            assertEquals("test2", value);
//
//        }

//        @Test
//        void basicPersistanceTestMultiple() throws IOException {
//
//            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
//            Instant now = Instant.now();
//
//            EClass model = nexus.createModel(TEST_MODEL, now);
//            EClass model2 = nexus.createModel("TestModelNew", now);
//            EReference service = nexus.createService(model, TESTSERVICE, now);
//            EReference service2 = nexus.createService(model2, "testservice2", now);
//            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
//                    null);
//            EAttribute resource2 = nexus.createResource(service2.getEReferenceType(), TEST_VALUE, String.class, now,
//                    null);
//            EClass model3 = nexus.createModel("something_else", now);
//            EReference service3 = nexus.createService(model3, "whatever", now);
//            EAttribute resource3 = nexus.createResource(service3.getEReferenceType(), TEST_VALUE, String.class, now,
//                    null);
//            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);
//            Provider p2 = nexus.createProviderInstance(EMFUtil.constructPackageUri("TestModelNew"), "TestModelNew",
//                    "testproviderNew", now);
//            Provider p3 = nexus.createProviderInstance(EMFUtil.constructPackageUri("something_else"), "something_else",
//                    "something_else", now);
//
//            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);
//            nexus.handleDataUpdate(p2, service2.getName(), service2, service2.getEReferenceType(), resource2, "test2",
//                    now);
//            nexus.handleDataUpdate(p3, service3.getName(), service3, service3.getEReferenceType(), resource3, "test2",
//                    now);
//
//            nexus.shutDown();
//
//            nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
//
//            assertObject(nexus, TEST_PKG, TEST_MODEL, TESTPROVIDER, TESTSERVICE, TEST_VALUE, "test");
//            assertObject(nexus, EMFUtil.constructPackageUri("TestModelNew"), "TestModelNew", "testproviderNew",
//                    "testservice2", TEST_VALUE, "test2");
//            assertObject(nexus, EMFUtil.constructPackageUri("something_else"), "something_else", "something_else",
//                    "whatever", TEST_VALUE, "test2");
//
//        }

        void assertObject(ModelNexus nexus, String modelPackageUri, String modelName, String provider, String service,
                String resource, String value) {
            Provider providerObject = nexus.getProvider(modelPackageUri, modelName, provider);
            assertNotNull(providerObject);
            EStructuralFeature serviceFeature = providerObject.eClass().getEStructuralFeature(service);
            Service serviceObject = (Service) providerObject.eGet(serviceFeature);

            EStructuralFeature valueFeature = serviceObject.eClass().getEStructuralFeature(resource);

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object valueObject = serviceObject.eGet(valueFeature);
            assertEquals(value, valueObject);
        }

        @Test
        void testFindProviderWithoutModel() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            Instant now = Instant.now();

            EClass model = nexus.createModel(TEST_MODEL, now);
            EReference service = nexus.createService(model, TESTSERVICE, TESTSERVICE, now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_VALUE, String.class, now,
                    null);
            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);

            assertEquals(TEST_MODEL, nexus.getProviderModel(TESTPROVIDER));

            Provider provider = nexus.getProvider(TESTPROVIDER);

            assertNotNull(provider);
            assertEquals(TESTPROVIDER, provider.getId());
            assertSame(nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER), provider);
        }

        @Test
        void testUnableToCreateProviderNoModel() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();

            assertThrows(IllegalArgumentException.class,
                    () -> nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now));
        }

        @Test
        void testUnableToCreateProviderDifferentModelAndClashingId() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();

            nexus.createModel(TEST_PKG, TEST_MODEL, now);
            nexus.createModel(TEST_PKG, "TestModel2", now);
            nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            assertThrows(IllegalArgumentException.class,
                    () -> nexus.createProviderInstance(TEST_PKG, "TestModel2", TESTPROVIDER, now));
        }

        @ParameterizedTest
        @ValueSource(strings = { "test", "123Test", "final", "http://test.de/asldjkhasdlj", "123$.final/-",
                "protected", })
        void testCreateModelWithStrangeName(String name) {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            nexus.createModel(name, Instant.now());
            Provider providerInstance = nexus.createProviderInstance(name, "test", Instant.now());
            System.out.println(name + " - " + providerInstance.eClass().getName());
            assertNotNull(providerInstance);
            assertEquals(name, EMFUtil.getModelName(providerInstance.eClass()));
            assertNotEquals(name, providerInstance.eClass().getName());
        }

        @Test
        void testDeleteModel() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            // Set up models and providers
            Instant now = Instant.now();

            EClass model = nexus.createModel(TEST_PKG, TEST_MODEL, now);
            EClass model2 = nexus.createModel(TEST_PKG, "TestModel2", now);

            EPackage ePackage = model.getEPackage();

            assertFalse(nexus.getModel(TEST_PKG, TEST_MODEL).isEmpty());
            assertFalse(nexus.getModel(TEST_PKG, "TestModel2").isEmpty());
            assertTrue(nexus.registered(model));
            assertTrue(nexus.registered(model2));

            nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            nexus.createProviderInstance(TEST_PKG, "TestModel2", TESTPROVIDER + "2");

            assertNotNull(nexus.getProvider(TESTPROVIDER));
            assertNotNull(nexus.getProvider(TESTPROVIDER + "2"));

            // Delete a model, the provider should also be deleted
            nexus.deleteModel(TEST_PKG, "TestModel2");

            assertFalse(nexus.getModel(TEST_PKG, TEST_MODEL).isEmpty());
            assertNotNull(nexus.getProvider(TESTPROVIDER));
            assertTrue(nexus.registered(model));

            assertTrue(nexus.getModel(TEST_PKG, "TestModel2").isEmpty());
            assertNull(nexus.getProvider(TESTPROVIDER + "2"));
            assertFalse(nexus.registered(model2));

            assertTrue(nexus.registered(ePackage));

            // Delete the second model, everything should be deleted
            nexus.deleteModel(TEST_PKG, TEST_MODEL);

            assertTrue(nexus.getModel(TEST_PKG, TEST_MODEL).isEmpty());
            assertNull(nexus.getProvider(TESTPROVIDER));
            assertFalse(nexus.registered(model));

            assertTrue(nexus.getModel(TEST_PKG, "TestModel2").isEmpty());
            assertNull(nexus.getProvider(TESTPROVIDER + "2"));
            assertFalse(nexus.registered(model2));

            assertFalse(nexus.registered(ePackage));
        }
    }

    @Nested
    public class LinkedProviderTests {

        @Test
        void addLink() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            Instant now = Instant.now();

            EClass model = nexus.createModel("TestModel", now);
            EReference service = nexus.createService(model, "testservice", "testservice", now);
            EReference service2 = nexus.createService(model, "testservice2", "testservice", now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), "testValue", String.class, now,
                    null);
            EAttribute resource2 = nexus.createResource(service2.getEReferenceType(), "testValue", String.class, now,
                    null);
            EClass model2 = nexus.createModel("something_else", now);
            EReference service3 = nexus.createService(model2, "whatever", "whatever", now);
            EAttribute resource3 = nexus.createResource(service3.getEReferenceType(), "testValue", String.class, now,
                    null);
            Provider p = nexus.createProviderInstance("TestModel", "testprovider", now);
            Provider p2 = nexus.createProviderInstance("TestModel", "testproviderNew", now);
            Provider p3 = nexus.createProviderInstance("something_else", "something_else", now);

            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, "test", now);
            nexus.handleDataUpdate(p2, service2.getName(), service2, service2.getEReferenceType(), resource2, "test2",
                    now);
            nexus.handleDataUpdate(p3, service3.getName(), service3, service3.getEReferenceType(), resource3, "test2",
                    now);

            nexus.linkProviders("testprovider", "testproviderNew", Instant.now());

            Provider provider = nexus.getProvider(EMFUtil.constructPackageUri("TestModel"), "TestModel",
                    "testprovider");

            assertEquals(1, provider.getLinkedProviders().size());

            nexus.linkProviders("testprovider", "something_else", Instant.now());

            assertEquals(2, provider.getLinkedProviders().size());
        }

        @Test
        void removeLink() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Instant now = Instant.now();

            EClass model = nexus.createModel("TestModel", now);
            EReference service = nexus.createService(model, "testservice", "testservice", now);
            EReference service2 = nexus.createService(model, "testservice2", "testservice", now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), "testValue", String.class, now,
                    null);
            EAttribute resource2 = nexus.createResource(service2.getEReferenceType(), "testValue", String.class, now,
                    null);
            EClass model2 = nexus.createModel("something_else", now);
            EReference service3 = nexus.createService(model2, "whatever", "whatever", now);
            EAttribute resource3 = nexus.createResource(service3.getEReferenceType(), "testValue", String.class, now,
                    null);
            Provider p = nexus.createProviderInstance("TestModel", "testprovider", now);
            Provider p2 = nexus.createProviderInstance("TestModel", "testproviderNew", now);
            Provider p3 = nexus.createProviderInstance("something_else", "something_else", now);

            nexus.handleDataUpdate(p, service.getName(), service, model2, resource, "test", now);
            nexus.handleDataUpdate(p2, service2.getName(), service2, model2, resource2, "test2", now);
            nexus.handleDataUpdate(p3, service3.getName(), service3, model2, resource3, "test2", now);

            nexus.linkProviders("testprovider", "testproviderNew", Instant.now());

            Provider provider = nexus.getProvider(EMFUtil.constructPackageUri("TestModel"), "TestModel",
                    "testprovider");

            assertEquals(1, provider.getLinkedProviders().size());

            nexus.linkProviders("testprovider", "something_else", Instant.now());

            assertEquals(2, provider.getLinkedProviders().size());

            nexus.unlinkProviders("testprovider", "testproviderNew", Instant.now());
            assertEquals(1, provider.getLinkedProviders().size());
        }
    }

    @Nested
    public class DynamicProviderTests {

        private EPackage ePackage;
        private ModelNexus nexus;

        @BeforeEach
        public void setup() throws IOException {
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

            nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Resource extendedPackageResource = resourceSet
                    .createResource(URI.createURI("https://eclipse.org/sensinact/test/1.0"));
            InputStream ín = getClass().getResourceAsStream("/model/extended.ecore");

            assertNotNull(ín);

            extendedPackageResource.load(ín, Collections.singletonMap(XMLResource.OPTION_URI_HANDLER, handler));

            ePackage = (EPackage) extendedPackageResource.getContents().get(0);

            assertNotNull(ePackage);

        }

        @Test
        void duplicatedServiceTest() {

            DynamicProvider provider = (DynamicProvider) EcoreUtil
                    .create((EClass) ePackage.getEClassifier("DynamicTemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Service testService3 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService4 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.getServices().put("testService1", testService3);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");
            testService3.eSet(testService3.eClass().getEStructuralFeature("foo"), "fizz");
            testService4.eSet(testService4.eClass().getEStructuralFeature("bar"), "buzz");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            assertThrows(IllegalArgumentException.class, () -> nexus.save(provider));
        }
    }

    @Nested
    public class AdminTests {

        private ModelNexus nexus;
        private EPackage ePackage;

        @BeforeEach
        public void setup() throws IOException {
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

            nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Resource extendedPackageResource = resourceSet
                    .createResource(URI.createURI("https://eclipse.org/sensinact/test/1.0"));
            InputStream ín = getClass().getResourceAsStream("/model/extended.ecore");

            assertNotNull(ín);

            extendedPackageResource.load(ín, Collections.singletonMap(XMLResource.OPTION_URI_HANDLER, handler));

            ePackage = (EPackage) extendedPackageResource.getContents().get(0);

            assertNotNull(ePackage);
        }

        /**
         * A Simple change of one attribute without a specific timestamp set
         */
        @Test
        void pushEObjectTestSimpleAttributeChange() throws IOException, InterruptedException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));

            provider.setId("sensor");

            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            Provider saved = nexus.save(provider);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);
            EClass testAdminEClass = (EClass) ePackage.getEClassifier("TestAdmin");
            Admin testAdmin = (Admin) EcoreUtil.create(testAdminEClass);
            saved.setAdmin(testAdmin);
            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider savedAgain = nexus.save(saved);

            assertNotNull(savedAgain.getAdmin());
            assertEquals(testAdminEClass, savedAgain.getAdmin().eClass());
        }
    }

    @Nested
    public class EPackageTests {

        private ModelNexus nexus;
        private EPackage ePackage;

        @BeforeEach
        public void setup() throws IOException {
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

            nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);

            Resource extendedPackageResource = resourceSet
                    .createResource(URI.createURI("https://eclipse.org/sensinact/test/1.0"));
            InputStream ín = getClass().getResourceAsStream("/model/extended.ecore");

            assertNotNull(ín);

            extendedPackageResource.load(ín, Collections.singletonMap(XMLResource.OPTION_URI_HANDLER, handler));

            ePackage = (EPackage) extendedPackageResource.getContents().get(0);

            assertNotNull(ePackage);
        }

        @Test
        void addModelEPackage() throws IOException, InterruptedException {
            EClassifier eClassifier = ePackage.getEClassifier("TemperatureSensor");
            String modelName = EMFUtil.getModelName((EClass) eClassifier);
            Optional<EClass> model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isEmpty());

            nexus.addEPackage(ePackage);

            model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isPresent());
        }

        @Test
        void removeModelEPackage() throws IOException, InterruptedException {
            EClassifier eClassifier = ePackage.getEClassifier("TemperatureSensor");
            String modelName = EMFUtil.getModelName((EClass) eClassifier);
            Optional<EClass> model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isEmpty());

            nexus.addEPackage(ePackage);

            model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isPresent());
            nexus.removeEPackage(ePackage);
            resourceSet.getPackageRegistry().remove(ePackage.getNsURI());

            model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isEmpty());
        }

        @Test
        void checkInstance() throws IOException, InterruptedException {
            EClassifier eClassifier = ePackage.getEClassifier("TemperatureSensor");
            String modelName = EMFUtil.getModelName((EClass) eClassifier);
            Optional<EClass> model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isEmpty());

            nexus.addEPackage(ePackage);

            model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isPresent());

            Provider provider = nexus.createProviderInstance(ePackage.getNsURI(), modelName, "test");

            assertNotNull(provider);
            assertEquals(eClassifier, provider.eClass());
        }

        @Test
        void checkInstanceRemovalOnPackageRemove() throws IOException, InterruptedException {
            EClassifier eClassifier = ePackage.getEClassifier("TemperatureSensor");
            String modelName = EMFUtil.getModelName((EClass) eClassifier);
            Optional<EClass> model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isEmpty());

            nexus.addEPackage(ePackage);

            model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isPresent());

            Provider provider = nexus.createProviderInstance(ePackage.getNsURI(), modelName, "test");

            assertNotNull(provider);

            nexus.removeEPackage(ePackage);
            resourceSet.getPackageRegistry().remove(ePackage.getNsURI());

            model = nexus.getModel(ePackage.getNsURI(), modelName);

            assertTrue(model.isEmpty());
            provider = nexus.getProvider(provider.getId());
            assertNull(provider);
        }

    }

    @Nested
    public class CollectionResourceTests {

        private static final String TEST_MODEL = "TestCollectionModel";
        private final String TEST_PKG = EMFUtil.constructPackageUri(TEST_MODEL);
        private static final String TESTPROVIDER = "testprovider";

        @Test
        void createAndUpdateListResource() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            Instant now = Instant.now();

            EClass model = nexus.createModel(TEST_PKG, TEST_MODEL, now);
            EReference service = nexus.createService(model, "testservice", "testservice", now);

            // Create a collection resource with upperBound = -1
            EAttribute resource = nexus.createResource(service.getEReferenceType(), "testList", String.class, now,
                    null, Map.of(), false, 0, false, 0, -1);

            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            // Update with a list of strings
            java.util.List<String> testData = java.util.List.of("value1", "value2", "value3");
            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, testData, now);

            // Verify the data was stored correctly
            Provider provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            assertNotNull(provider);

            EStructuralFeature serviceFeature = model.getEStructuralFeature(service.getName());
            assertNotNull(serviceFeature);

            Service svc = (Service) provider.eGet(serviceFeature);
            assertNotNull(svc);

            EStructuralFeature rcFeature = service.getEReferenceType().getEStructuralFeature(resource.getName());
            assertNotNull(rcFeature);

            Object value = svc.eGet(rcFeature);
            assertNotNull(value);
            assertTrue(value instanceof Collection, "Value should be a Collection");

            @SuppressWarnings("unchecked")
            Collection<String> collectionValue = (Collection<String>) value;
            assertEquals(3, collectionValue.size());
            assertTrue(collectionValue.contains("value1"));
            assertTrue(collectionValue.contains("value2"));
            assertTrue(collectionValue.contains("value3"));
        }

        @Test
        void updateListResourceMultipleTimes() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            Instant now = Instant.now();

            EClass model = nexus.createModel(TEST_PKG, TEST_MODEL, now);
            EReference service = nexus.createService(model, "testservice", "testservice", now);

            // Create a collection resource
            EAttribute resource = nexus.createResource(service.getEReferenceType(), "testList", Integer.class, now,
                    null, Map.of(), false, 0, false, 0, -1);

            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            // First update
            java.util.List<Integer> firstData = java.util.List.of(1, 2, 3);
            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, firstData, now);

            Provider provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            EStructuralFeature serviceFeature = model.getEStructuralFeature(service.getName());
            Service svc = (Service) provider.eGet(serviceFeature);
            EStructuralFeature rcFeature = service.getEReferenceType().getEStructuralFeature(resource.getName());

            @SuppressWarnings("unchecked")
            Collection<Integer> value1 = (Collection<Integer>) svc.eGet(rcFeature);
            assertEquals(3, value1.size());

            // Second update - replace with different values
            java.util.List<Integer> secondData = java.util.List.of(10, 20);
            Instant later = now.plusSeconds(60);
            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, secondData, later);

            provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            svc = (Service) provider.eGet(serviceFeature);

            @SuppressWarnings("unchecked")
            Collection<Integer> value2 = (Collection<Integer>) svc.eGet(rcFeature);
            assertEquals(2, value2.size());
            assertTrue(value2.contains(10));
            assertTrue(value2.contains(20));
            assertFalse(value2.contains(1));
        }

        @Test
        void emptyListResource() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            Instant now = Instant.now();

            EClass model = nexus.createModel(TEST_PKG, TEST_MODEL, now);
            EReference service = nexus.createService(model, "testservice", "testservice", now);

            // Create a collection resource
            EAttribute resource = nexus.createResource(service.getEReferenceType(), "testList", String.class, now,
                    null, Map.of(), false, 0, false, 0, -1);

            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            // Update with an empty list
            java.util.List<String> emptyData = java.util.List.of();
            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, emptyData, now);

            Provider provider = nexus.getProvider(TEST_PKG, TEST_MODEL, TESTPROVIDER);
            EStructuralFeature serviceFeature = model.getEStructuralFeature(service.getName());
            Service svc = (Service) provider.eGet(serviceFeature);
            EStructuralFeature rcFeature = service.getEReferenceType().getEStructuralFeature(resource.getName());

            Object value = svc.eGet(rcFeature);
            assertNotNull(value);
            assertTrue(value instanceof Collection);

            @SuppressWarnings("unchecked")
            Collection<String> collectionValue = (Collection<String>) value;
            assertEquals(0, collectionValue.size());
        }

        @Test
        void saveAndMergeListResourceChange() {
            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            Instant now = Instant.now();

            EClass model = nexus.createModel(TEST_PKG, TEST_MODEL, now);
            EReference service = nexus.createService(model, "testservice", "testservice", now);

            // Create a collection resource
            EAttribute resource = nexus.createResource(service.getEReferenceType(), "testList", String.class, now,
                    null, Map.of(), false, 0, false, 0, -1);

            Provider p = nexus.createProviderInstance(TEST_PKG, TEST_MODEL, TESTPROVIDER, now);

            // Initial update with a list
            java.util.List<String> initialData = java.util.List.of("a", "b", "c");
            nexus.handleDataUpdate(p, service.getName(), service, service.getEReferenceType(), resource, initialData, now);

            // Save the provider
            Provider saved = nexus.save(p);

            Mockito.clearInvocations(accumulator);

            // Get the service and modify the list
            EStructuralFeature serviceFeature = model.getEStructuralFeature(service.getName());
            Service svc = (Service) saved.eGet(serviceFeature);
            EStructuralFeature rcFeature = service.getEReferenceType().getEStructuralFeature(resource.getName());

            @SuppressWarnings("unchecked")
            EList<String> list = (EList<String>) svc.eGet(rcFeature);
            list.clear();
            list.add("x");
            list.add("y");

            // Update the metadata timestamp to indicate the change
            Instant later = now.plusSeconds(60);
            svc.getMetadata().get(resource).setTimestamp(later);

            // Save again - this triggers EMFCompareUtil to detect changes
            Provider savedAgain = nexus.save(saved);

            // Verify the list was updated
            svc = (Service) savedAgain.eGet(serviceFeature);
            @SuppressWarnings("unchecked")
            Collection<String> updatedList = (Collection<String>) svc.eGet(rcFeature);
            assertEquals(2, updatedList.size());
            assertTrue(updatedList.contains("x"));
            assertTrue(updatedList.contains("y"));
        }
    }
}

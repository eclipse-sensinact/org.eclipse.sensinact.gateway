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
package org.eclipse.sensinact.prototype.model.nexus.impl;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

        @Test
        void basicTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            Provider provider = nexus.getProvider("TestModel", "testprovider");
            assertNotNull(provider);

            assertNotNull(provider.getAdmin());
            assertEquals("testprovider", provider.getAdmin().getFriendlyName());

            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("testservice");
            assertNotNull(serviceFeature);
            assertEquals("testservice", serviceFeature.getName());
            EClass eClass = (EClass) serviceFeature.getEType();
            assertEquals("TestModelTestservice", eClass.getName());

            Service service = (Service) provider.eGet(serviceFeature);

            assertNotNull(service);

            EStructuralFeature valueFeature = eClass.getEStructuralFeature("testValue");

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object value = service.eGet(valueFeature);
            assertEquals("test", value);
        }

        @Test
        void sensiNactProvider() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            List<Provider> providers = nexus.getProviders();

            assertEquals(1, providers.size());
            Provider provider = providers.get(0);

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
            assertEquals(startedFeature.getEType(), SensiNactPackage.eINSTANCE.getEInstant());

            Instant started = (Instant) service.eGet(startedFeature);
            assertNotNull(started);
            assertEquals(Instant.now().truncatedTo(DAYS), started.truncatedTo(DAYS));
        }

        @Test
        void basicServiceExtensionTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            Provider provider = nexus.getProvider("TestModel", "testprovider");
            assertNotNull(provider);
            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("testservice");
            Service service = (Service) provider.eGet(serviceFeature);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue2", String.class, "test",
                    Instant.now());

            Provider updatedProvider = nexus.getProvider("TestModel", "testprovider");
            assertEquals(provider, updatedProvider);

            Service updateService = (Service) updatedProvider.eGet(serviceFeature);
            assertNotEquals(service, updateService);

            EStructuralFeature valueFeature = updateService.eClass().getEStructuralFeature("testValue2");

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object value = updateService.eGet(valueFeature);
            assertEquals("test", value);
        }

        @Test
        void basicSecondServiceTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            Provider provider = nexus.getProvider("TestModel", "testprovider");
            assertNotNull(provider);
            nexus.handleDataUpdate("TestModel", "testprovider", "testservice2", "testValue", String.class, "test2",
                    Instant.now());
            Provider updatedProvider = nexus.getProvider("TestModel", "testprovider");

            assertNotEquals(provider, updatedProvider);

            EStructuralFeature serviceFeature = updatedProvider.eClass().getEStructuralFeature("testservice2");
            Service service = (Service) updatedProvider.eGet(serviceFeature);

            EStructuralFeature valueFeature = service.eClass().getEStructuralFeature("testValue");

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object value = service.eGet(valueFeature);
            assertEquals("test2", value);
        }

        @Test
        void basicPersistanceTest() throws IOException {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice2", "testValue", String.class, "test2",
                    Instant.now());

            nexus.shutDown();

            nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
            Provider provider = nexus.getProvider("TestModel", "testprovider");
            assertNotNull(provider);
            EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("testservice2");
            Service service = (Service) provider.eGet(serviceFeature);

            EStructuralFeature valueFeature = service.eClass().getEStructuralFeature("testValue");

            assertNotNull(valueFeature);
            assertEquals(valueFeature.getEType(), EcorePackage.Literals.ESTRING);

            Object value = service.eGet(valueFeature);
            assertEquals("test2", value);

        }

        @Test
        void basicPersistanceTestMultiple() throws IOException {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            nexus.handleDataUpdate("TestModelNew", "testproviderNew", "testservice2", "testValue", String.class,
                    "test2", Instant.now());

            nexus.handleDataUpdate(null, "something_else", "whatever", "testValue", String.class, "test2",
                    Instant.now());

            nexus.shutDown();

            nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            assertObject(nexus, "TestModel", "testprovider", "testservice", "testValue", "test");
            assertObject(nexus, "TestModelNew", "testproviderNew", "testservice2", "testValue", "test2");
            assertObject(nexus, "something_else", "something_else", "whatever", "testValue", "test2");

        }

        void assertObject(ModelNexus nexus, String modelName, String provider, String service, String resource,
                String value) {
            Provider providerObject = nexus.getProvider(modelName, provider);
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
            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            assertEquals("TestModel", nexus.getProviderModel("testprovider"));

            Provider provider = nexus.getProvider("testprovider");

            assertNotNull(provider);
            assertEquals("testprovider", provider.getId());
            assertSame(nexus.getProvider("TestModel", "testprovider"), provider);
        }

        @Test
        void testUnableToCreateProviderDifferentModelAndClashingId() {
            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            assertThrows(IllegalArgumentException.class, () -> nexus.handleDataUpdate("TestModel2", "testprovider",
                    "testservice", "testValue", String.class, "test", Instant.now()));
        }
    }

    @Nested
    public class LinkedProviderTests {

        @Test
        void addLink() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            nexus.handleDataUpdate("TestModelNew", "testproviderNew", "testservice2", "testValue", String.class,
                    "test2", Instant.now());

            nexus.handleDataUpdate(null, "something_else", "whatever", "testValue", String.class, "test2",
                    Instant.now());

            nexus.linkProviders("TestModel", "testprovider", "TestModelNew", "testproviderNew", Instant.now());

            Provider provider = nexus.getProvider("TestModel", "testprovider");

            assertEquals(1, provider.getLinkedProviders().size());

            nexus.linkProviders("TestModel", "testprovider", null, "something_else", Instant.now());

            assertEquals(2, provider.getLinkedProviders().size());
        }

        @Test
        void removeLink() {

            ModelNexus nexus = new ModelNexus(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

            nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test",
                    Instant.now());

            nexus.handleDataUpdate("TestModelNew", "testproviderNew", "testservice2", "testValue", String.class,
                    "test2", Instant.now());

            nexus.handleDataUpdate(null, "something_else", "whatever", "testValue", String.class, "test2",
                    Instant.now());

            nexus.linkProviders("TestModel", "testprovider", "TestModelNew", "testproviderNew", Instant.now());

            Provider provider = nexus.getProvider("TestModel", "testprovider");

            assertEquals(1, provider.getLinkedProviders().size());

            nexus.linkProviders("TestModel", "testprovider", null, "something_else", Instant.now());

            assertEquals(2, provider.getLinkedProviders().size());

            nexus.unlinkProviders("TestModel", "testprovider", "TestModelNew", "testproviderNew", Instant.now());
            assertEquals(1, provider.getLinkedProviders().size());
        }
    }
}

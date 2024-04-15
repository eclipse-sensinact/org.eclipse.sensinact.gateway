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
package org.eclipse.sensinact.core.model.nexus.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.URIMappingRegistryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.model.core.metadata.ResourceMetadata;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;
import org.eclipse.sensinact.model.core.provider.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderFactory;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Juergen Albert
 * @since 10 Oct 2022
 */
@ExtendWith(MockitoExtension.class)
public class SubscriptionTest {

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
    public class BasicEventsTest {

        private static final String TEST_MODEL = "testmodel";
        private static final String TEST_PROVIDER = "testprovider";
        private static final String TEST_SERVICE = "testservice";
        private static final String TEST_SERVICE_2 = "testservice2";
        private static final String TEST_RESOURCE = "testValue";
        private static final String TEST_RESOURCE_2 = "testValue2";
        private static final String TEST_VALUE = "test";
        private static final String TEST_VALUE_2 = "test2";

        private final String TEST_MODEL_PKG = EMFUtil.constructPackageUri(TEST_MODEL);

        @Test
        void basicTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            // Ignore the setup of the sensiNact provider
            Mockito.clearInvocations(accumulator);

            Instant now = Instant.now();
            EClass model = nexus.createModel(TEST_MODEL, now);
            EReference service = nexus.createService(model, TEST_SERVICE, now);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_RESOURCE, String.class, now,
                    null);
            Provider p = nexus.createProviderInstance(TEST_MODEL, TEST_PROVIDER, now);

            nexus.handleDataUpdate(p, service, resource, TEST_VALUE, now);

            Mockito.verify(accumulator).addProvider(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER);
            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName());
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName());
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), String.class, null, TEST_PROVIDER, now);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), null,
                    Map.of("value", TEST_PROVIDER, "timestamp", now), now);
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName());
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL.getName());
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), String.class, null,
                    model.getEPackage().getNsURI(), now);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(), ProviderPackage.Literals.ADMIN__MODEL.getName(),
                    String.class, null, EMFUtil.getModelName(model), now);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), null,
                    Map.of("value", model.getEPackage().getNsURI(), "timestamp", now), now);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(), ProviderPackage.Literals.ADMIN__MODEL.getName(),
                    null, Map.of("value", EMFUtil.getModelName(model), "timestamp", now), now);

            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE);
            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE);
            // TODO - this is missing
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE, String.class, null, TEST_VALUE, now);
            // TODO - the value is in here, which is surprising, as is the timestamp being a
            // date
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE, null, Map.of("value", TEST_VALUE, "timestamp", now), now);

            Mockito.verifyNoMoreInteractions(accumulator);
        }

        @Test
        void basicServiceExtensionTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            // Ignore the setup of the sensiNact provider
            Mockito.clearInvocations(accumulator);

            Instant now = Instant.now();
            Instant before = now.minus(Duration.ofHours(1));

            EClass model = nexus.createModel(TEST_MODEL, before);
            EReference service = nexus.createService(model, TEST_SERVICE, before);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_RESOURCE, String.class, before,
                    null);
            EAttribute resource2 = nexus.createResource(service.getEReferenceType(), TEST_RESOURCE_2, String.class,
                    before, null);
            Provider p = nexus.createProviderInstance(TEST_MODEL, TEST_PROVIDER, before);

            nexus.handleDataUpdate(p, service, resource, TEST_VALUE, before);
            nexus.handleDataUpdate(p, service, resource2, TEST_VALUE, now);

            Mockito.verify(accumulator).addProvider(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER);
            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName());
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName());
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), String.class, null, TEST_PROVIDER, before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), null,
                    Map.of("value", TEST_PROVIDER, "timestamp", before), before);
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName());
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL.getName());
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), String.class, null,
                    model.getEPackage().getNsURI(), before);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(), ProviderPackage.Literals.ADMIN__MODEL.getName(),
                    String.class, null, EMFUtil.getModelName(model), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), null,
                    Map.of("value", model.getEPackage().getNsURI(), "timestamp", before), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(), ProviderPackage.Literals.ADMIN__MODEL.getName(),
                    null, Map.of("value", EMFUtil.getModelName(model), "timestamp", before), before);

            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE);
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE);
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE_2);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE, String.class, null, TEST_VALUE, before);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE_2, String.class, null, TEST_VALUE, now);

            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE, null, Map.of("value", TEST_VALUE, "timestamp", before), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE_2, null, Map.of("value", TEST_VALUE, "timestamp", now), now);

            Mockito.verifyNoMoreInteractions(accumulator);
        }

        @Test
        void basicSecondServiceTest() {

            ModelNexus nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator);
            // Ignore the setup of the sensiNact provider
            Mockito.clearInvocations(accumulator);

            Instant now = Instant.now();
            Instant before = now.minus(Duration.ofHours(1));

            EClass model = nexus.createModel(TEST_MODEL, before);
            EReference service = nexus.createService(model, TEST_SERVICE, before);
            EReference service2 = nexus.createService(model, TEST_SERVICE_2, before);
            EAttribute resource = nexus.createResource(service.getEReferenceType(), TEST_RESOURCE, String.class, before,
                    null);
            EAttribute resource2 = nexus.createResource(service2.getEReferenceType(), TEST_RESOURCE_2, String.class,
                    before, null);
            Provider p = nexus.createProviderInstance(TEST_MODEL, TEST_PROVIDER, before);

            nexus.handleDataUpdate(p, service, resource, TEST_VALUE, before);
            nexus.handleDataUpdate(p, service2, resource2, TEST_VALUE_2, now);

            Mockito.verify(accumulator).addProvider(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER);
            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName());
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName());
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), String.class, null, TEST_PROVIDER, before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), null,
                    Map.of("value", TEST_PROVIDER, "timestamp", before), before);
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName());
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL.getName());
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), String.class, null,
                    model.getEPackage().getNsURI(), before);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(), ProviderPackage.Literals.ADMIN__MODEL.getName(),
                    String.class, null, EMFUtil.getModelName(model), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), null,
                    Map.of("value", model.getEPackage().getNsURI(), "timestamp", before), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER,
                    ProviderPackage.Literals.PROVIDER__ADMIN.getName(), ProviderPackage.Literals.ADMIN__MODEL.getName(),
                    null, Map.of("value", EMFUtil.getModelName(model), "timestamp", before), before);

            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE);
            Mockito.verify(accumulator).addService(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE_2);
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE);
            Mockito.verify(accumulator).addResource(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE_2,
                    TEST_RESOURCE_2);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE, String.class, null, TEST_VALUE, before);
            Mockito.verify(accumulator).resourceValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE_2,
                    TEST_RESOURCE_2, String.class, null, TEST_VALUE_2, now);

            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE,
                    TEST_RESOURCE, null, Map.of("value", TEST_VALUE, "timestamp", before), before);
            Mockito.verify(accumulator).metadataValueUpdate(TEST_MODEL_PKG, TEST_MODEL, TEST_PROVIDER, TEST_SERVICE_2,
                    TEST_RESOURCE_2, null, Map.of("value", TEST_VALUE_2, "timestamp", now), now);

            Mockito.verifyNoMoreInteractions(accumulator);
        }
    }

    @Nested
    public class EObjectPushTests {

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
        void pushEObjectTestSimple() throws IOException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            verifyNewProviderNotification(accumulator, saved);
        }

        @Test
        void pushEObjectTestSimpleWithServiceMap() throws IOException {

            DynamicProvider provider = (DynamicProvider) EcoreUtil
                    .create((EClass) ePackage.getEClassifier("DynamicTemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));
            Service testService3 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService4 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService3.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService4.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            provider.getServices().put("testService3", testService3);
            provider.getServices().put("testService4", testService4);

            Provider saved = nexus.save(provider);

            verifyNewProviderNotification(accumulator, saved);
        }

        @Test
        void pushEObjectTestSimpleWithServiceMapRemoveService() throws IOException {

            DynamicProvider provider = (DynamicProvider) EcoreUtil
                    .create((EClass) ePackage.getEClassifier("DynamicTemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));
            Service testService3 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService4 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService3.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService4.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            provider.getServices().put("testService3", testService3);
            provider.getServices().put("testService4", testService4);

            Provider saved = nexus.save(provider);
            assertTrue(saved instanceof DynamicProvider);
            verifyNewProviderNotification(accumulator, saved);

            DynamicProvider dynamic = (DynamicProvider) saved;
            Service service = dynamic.getServices().removeKey("testService4");
            dynamic.getServices().put("bla", service);

            saved = nexus.save(dynamic);

            verifyProviderUpdateNotification(accumulator, provider, saved);
        }

        /**
         * Attribute is new; use their timestamp
         */
        @Test
        void pushEObjectTestSimpleAttributeAddWithSetTimestamp() throws IOException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eSet(testService1.eClass().getEStructuralFeature("foo2"), "somethingElse");
            Metadata newMetadata = ProviderFactory.eINSTANCE.createMetadata();
            newMetadata.setTimestamp(Instant.now());
            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1"))).getMetadata()
                    .put(testService1.eClass().getEStructuralFeature("foo2"), newMetadata);

            Instant mark = Instant.now();

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            Provider toTest = nexus.save(saved);

            Metadata curMetadata = ((Service) toTest.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));

            assertNotNull(curMetadata);
            assertFalse(curMetadata.getTimestamp().isAfter(mark));

            String modelName = EMFUtil.getModelName(provider.eClass());

            Mockito.verify(accumulator).addResource(ePackage.getNsURI(), modelName, provider.getId(), "testService1",
                    "foo2");
            Mockito.verify(accumulator).resourceValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo2", String.class, null, "somethingElse", newMetadata.getTimestamp());
            Mockito.verify(accumulator).metadataValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo2", null,
                    Map.of("value", "somethingElse", "timestamp", newMetadata.getTimestamp()),
                    newMetadata.getTimestamp());
            Mockito.verifyNoMoreInteractions(accumulator);
        }

        /**
         * A Simple change of one attribute without a specific timestamp set
         */
        @Test
        void pushEObjectTestSimpleAttributeChange() throws IOException, InterruptedException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eSet(testService1.eClass().getEStructuralFeature("foo"), "foo2");

            Metadata oldMetadata = ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));
            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1"))).getMetadata().clear();

            Instant mark = Instant.now();

            Thread.sleep(100);

            Provider toTest = nexus.save(saved);

            Metadata curMetadata = ((Service) toTest.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));

            assertNotNull(curMetadata);
            assertTrue(curMetadata.getTimestamp().isAfter(mark));

            String modelName = EMFUtil.getModelName(provider.eClass());

            Mockito.verify(accumulator).resourceValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo", String.class, "foo", "foo2", curMetadata.getTimestamp());
            Mockito.verify(accumulator).metadataValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo", Map.of("value", "foo", "timestamp", oldMetadata.getTimestamp()),
                    Map.of("value", "foo2", "timestamp", curMetadata.getTimestamp()), curMetadata.getTimestamp());
            Mockito.verifyNoMoreInteractions(accumulator);
        }

        /**
         * A Simple change of one attribute without a specific timestamp set
         */
        @Test
        void pushEObjectMetadataIsBecomesResourceMetadata() throws IOException, InterruptedException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            Metadata initialMetadata = ProviderFactory.eINSTANCE.createMetadata();
            initialMetadata.setTimestamp(Instant.now());
            testService1.getMetadata().put(testService1.eClass().getEStructuralFeature("foo"), initialMetadata);

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            Metadata initialMetadataToCheck = ((Service) saved
                    .eGet(provider.eClass().getEStructuralFeature("testService1"))).getMetadata()
                    .get(testService1.eClass().getEStructuralFeature("foo"));

            assertNotNull(initialMetadataToCheck);
            assertTrue(initialMetadataToCheck instanceof ResourceMetadata);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eSet(testService1.eClass().getEStructuralFeature("foo2"), "somethingElse");
            Metadata newMetadata = ProviderFactory.eINSTANCE.createMetadata();
            newMetadata.setTimestamp(Instant.now());
            FeatureCustomMetadata fcm = ProviderFactory.eINSTANCE.createFeatureCustomMetadata();
            fcm.setName("test.meta.1");
            fcm.setValue("some Test");
            fcm.setTimestamp(Instant.now());
            newMetadata.getExtra().add(fcm);
            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1"))).getMetadata()
                    .put(testService1.eClass().getEStructuralFeature("foo2"), newMetadata);

            Instant mark = Instant.now();

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            Provider toTest = nexus.save(saved);

            Metadata curMetadata = ((Service) toTest.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));

            assertNotNull(curMetadata);
            assertFalse(curMetadata.getTimestamp().isAfter(mark));
            assertTrue(curMetadata instanceof ResourceMetadata);

            String modelName = EMFUtil.getModelName(provider.eClass());

            Mockito.verify(accumulator).addResource(ePackage.getNsURI(), modelName, provider.getId(), "testService1",
                    "foo2");
            Mockito.verify(accumulator).resourceValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo2", String.class, null, "somethingElse", newMetadata.getTimestamp());
            Mockito.verify(accumulator).metadataValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo2", null, Map.of("test.meta.1", "some Test", "value", "somethingElse",
                            "timestamp", newMetadata.getTimestamp()),
                    newMetadata.getTimestamp());
            Mockito.verifyNoMoreInteractions(accumulator);
        }

        /**
         * A Simple change of one attribute without a specific timestamp set
         */
        @Test
        void pushEObjectTestSimpleAttributeChangeWithMetadata() throws IOException, InterruptedException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eSet(testService1.eClass().getEStructuralFeature("foo"), "foo2");

            Metadata oldMetadata = ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));

            FeatureCustomMetadata fcm = ProviderFactory.eINSTANCE.createFeatureCustomMetadata();
            fcm.setName("test.meta.1");
            fcm.setValue("some Test");
            fcm.setTimestamp(Instant.now());

            oldMetadata.getExtra().add(fcm);

            fcm = ProviderFactory.eINSTANCE.createFeatureCustomMetadata();
            fcm.setName("test.meta.2");
            fcm.setValue(2);
            fcm.setTimestamp(Instant.now());

            Instant mark = Instant.now();
            Thread.sleep(100);

            oldMetadata.getExtra().add(fcm);

            Instant oldMetadataTimestampToCompare = oldMetadata.getTimestamp();

            oldMetadata.setTimestamp(Instant.now());

            Thread.sleep(100);

            Provider toTest = nexus.save(saved);

            Metadata curMetadata = ((Service) toTest.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));

            System.err.println(mark);
            System.err.println(curMetadata.getTimestamp());

            assertNotNull(curMetadata);
            assertTrue(curMetadata.getTimestamp().isAfter(mark));

            String modelName = EMFUtil.getModelName(provider.eClass());

            Mockito.verify(accumulator).resourceValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo", String.class, "foo", "foo2", curMetadata.getTimestamp());
            Mockito.verify(accumulator).metadataValueUpdate(
                    ePackage.getNsURI(), modelName, provider.getId(), "testService1", "foo",
                    Map.of("value", "foo", "timestamp", oldMetadataTimestampToCompare), Map.of("test.meta.1",
                            "some Test", "test.meta.2", 2, "value", "foo2", "timestamp", curMetadata.getTimestamp()),
                    curMetadata.getTimestamp());
            Mockito.verifyNoMoreInteractions(accumulator);
        }

        /**
         * A Simple change of one attribute without a specific timestamp set
         */
        @Test
        void pushEObjectTestSimpleAttributeChangeWithModelAnnotationMetadata()
                throws IOException, InterruptedException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService2")))
                    .eSet(testService2.eClass().getEStructuralFeature("annotated"), "avalue");

            Instant mark = Instant.now();
            Thread.sleep(100);

            Provider toTest = nexus.save(saved);

            Metadata curMetadata = ((Service) toTest.eGet(provider.eClass().getEStructuralFeature("testService2")))
                    .getMetadata().get(testService2.eClass().getEStructuralFeature("annotated"));

            System.err.println(mark);
            System.err.println(curMetadata.getTimestamp());

            assertNotNull(curMetadata);
            assertTrue(curMetadata.getTimestamp().isAfter(mark));
            assertEquals(0, curMetadata.getExtra().size());

            String modelName = EMFUtil.getModelName(provider.eClass());

            Mockito.verify(accumulator).addResource(ePackage.getNsURI(), modelName, provider.getId(), "testService2",
                    "annotated");
            Mockito.verify(accumulator).resourceValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService2", "annotated", String.class, null, "avalue", curMetadata.getTimestamp());
            Mockito.verify(accumulator).metadataValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService2", "annotated", null, Map.of("test", "testMetadata", "test2", "testMetadata2", "value",
                            "avalue", "timestamp", curMetadata.getTimestamp()),
                    curMetadata.getTimestamp());
            Mockito.verifyNoMoreInteractions(accumulator);
        }

        /**
         * A Simple change of one attribute without a specific timestamp set
         */
        @Test
        void pushEObjectTestSimpleAttributeChangeWithModelAnnotationMetadataWithOverwrite()
                throws IOException, InterruptedException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService2")))
                    .eSet(testService2.eClass().getEStructuralFeature("annotated"), "avalue");

            Metadata overwrite = ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService2")))
                    .getMetadata().get(testService2.eClass().getEStructuralFeature("annotated"));

            assertNull(overwrite);

            overwrite = ProviderFactory.eINSTANCE.createMetadata();
            FeatureCustomMetadata fcm = ProviderFactory.eINSTANCE.createFeatureCustomMetadata();
            fcm.setName("test");
            fcm.setValue("Something different");
            fcm.setTimestamp(Instant.now());
            overwrite.getExtra().add(fcm);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService2"))).getMetadata()
                    .put(testService2.eClass().getEStructuralFeature("annotated"), overwrite);

            Instant mark = Instant.now();
            Thread.sleep(100);

            Provider toTest = nexus.save(saved);

            Metadata curMetadata = ((Service) toTest.eGet(provider.eClass().getEStructuralFeature("testService2")))
                    .getMetadata().get(testService2.eClass().getEStructuralFeature("annotated"));

            System.err.println(mark);
            System.err.println(curMetadata.getTimestamp());

            assertNotNull(curMetadata);
            assertTrue(curMetadata.getTimestamp().isAfter(mark));
            assertNotEquals(overwrite, curMetadata);
            assertEquals(1, curMetadata.getExtra().size());
            assertEquals("test", curMetadata.getExtra().get(0).getName());
            assertEquals(fcm.getValue(), curMetadata.getExtra().get(0).getValue());

            String modelName = EMFUtil.getModelName(provider.eClass());

            Mockito.verify(accumulator).addResource(ePackage.getNsURI(), modelName, provider.getId(), "testService2",
                    "annotated");
            Mockito.verify(accumulator).resourceValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService2", "annotated", String.class, null, "avalue", curMetadata.getTimestamp());
            Mockito.verify(accumulator).metadataValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService2", "annotated", null, Map.of("test", fcm.getValue(), "test2", "testMetadata2", "value",
                            "avalue", "timestamp", curMetadata.getTimestamp()),
                    curMetadata.getTimestamp());
            Mockito.verifyNoMoreInteractions(accumulator);
        }

        /**
         * remove of additional metadata
         */
        @Test
        void pushEObjectTestSimpleAttributeChangeWithRemoveOfExtraMetadata() throws IOException, InterruptedException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Metadata metadata = ProviderFactory.eINSTANCE.createMetadata();
            testService1.getMetadata().put(testService1.eClass().getEStructuralFeature("foo"), metadata);
            FeatureCustomMetadata fcm = ProviderFactory.eINSTANCE.createFeatureCustomMetadata();
            fcm.setName("test.meta.1");
            fcm.setValue("some Test");
            fcm.setTimestamp(Instant.now());

            metadata.getExtra().add(fcm);

            fcm = ProviderFactory.eINSTANCE.createFeatureCustomMetadata();
            fcm.setName("test.meta.2");
            fcm.setValue(2);
            fcm.setTimestamp(Instant.now());

            metadata.getExtra().add(fcm);

            Provider saved = nexus.save(provider);

            Mockito.clearInvocations(accumulator);
            Mockito.verifyNoMoreInteractions(accumulator);

            Metadata oldMetadata = ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));

            oldMetadata.getExtra().remove(1);

            Instant mark = Instant.now();
            Thread.sleep(100);

            Instant oldMetadataTimestampToCompare = oldMetadata.getTimestamp();

            oldMetadata.setTimestamp(Instant.now());

            Thread.sleep(100);

            Provider toTest = nexus.save(saved);

            Metadata curMetadata = ((Service) toTest.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .getMetadata().get(testService1.eClass().getEStructuralFeature("foo"));

            System.err.println(mark);
            System.err.println(curMetadata.getTimestamp());

            assertNotNull(curMetadata);
            assertTrue(curMetadata.getTimestamp().isAfter(mark));

            String modelName = EMFUtil.getModelName(provider.eClass());

            Mockito.verify(accumulator).resourceValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo", String.class, "foo", "foo", curMetadata.getTimestamp());
            Mockito.verify(accumulator).metadataValueUpdate(ePackage.getNsURI(), modelName, provider.getId(),
                    "testService1", "foo",
                    Map.of("test.meta.1", "some Test", "test.meta.2", 2, "value", "foo", "timestamp",
                            oldMetadataTimestampToCompare),
                    Map.of("test.meta.1", "some Test", "value", "foo", "timestamp", curMetadata.getTimestamp()),
                    curMetadata.getTimestamp());
            Mockito.verifyNoMoreInteractions(accumulator);
        }

        @Test
        void pushEObjectUpateAdminNotRemoved() throws IOException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));

            provider.setId("sensor");

            Provider saved = nexus.save(provider);

            saved.setAdmin(null);

            Provider test = nexus.save(saved);
            assertNotNull(test.getAdmin());
        }

        @Test
        void pushEObjectTestAttributeUpdates() throws IOException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            verifyNewProviderNotification(accumulator, saved);

            stripMetadata(saved);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eSet(testService1.eClass().getEStructuralFeature("foo"), "foo2");

            Provider modified = nexus.save(saved);

            verifyProviderUpdateNotification(accumulator, saved, modified);

        }

        @Test
        void pushEObjectTestAttributeUpdatesMultiple() throws IOException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            verifyNewProviderNotification(accumulator, saved);

            stripMetadata(saved);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eSet(testService1.eClass().getEStructuralFeature("foo"), "foo2");
            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService2")))
                    .eSet(testService2.eClass().getEStructuralFeature("bar"), "bar2");

            Provider modified = nexus.save(saved);

            verifyProviderUpdateNotification(accumulator, saved, modified);
        }

        @Test
        void pushEObjectTestAttributeUpdatesMultipleWithServiceMap() throws IOException {

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
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);
            provider.getServices().put("testService3", testService3);
            provider.getServices().put("testService4", testService4);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");
            testService3.eSet(testService3.eClass().getEStructuralFeature("foo"), "fizz");
            testService4.eSet(testService4.eClass().getEStructuralFeature("bar"), "buzz");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            DynamicProvider saved = (DynamicProvider) nexus.save(provider);

            verifyNewProviderNotification(accumulator, saved);

            stripMetadata(saved);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eSet(testService1.eClass().getEStructuralFeature("foo"), "foo2");
            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService2")))
                    .eSet(testService2.eClass().getEStructuralFeature("bar"), "bar2");
            saved.getServices().get("testService3").eSet(testService1.eClass().getEStructuralFeature("foo"), "fizz2");
            saved.getServices().get("testService4").eSet(testService2.eClass().getEStructuralFeature("bar"), "buzz2");

            Provider modified = nexus.save(saved);

            verifyProviderUpdateNotification(accumulator, saved, modified);
        }

        @Test
        void pushEObjectTestAttributeRemove() throws IOException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            verifyNewProviderNotification(accumulator, saved);

            stripMetadata(saved);

            ((Service) saved.eGet(provider.eClass().getEStructuralFeature("testService1")))
                    .eUnset(testService1.eClass().getEStructuralFeature("foo"));

            Provider modified = nexus.save(saved);

            verifyProviderUpdateNotification(accumulator, saved, modified);
        }

        @Test
        void pushEObjectTestAttributeWithMetadata() throws IOException {

            Provider provider = (Provider) EcoreUtil.create((EClass) ePackage.getEClassifier("TemperatureSensor"));
            Service testService1 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService1"));
            Service testService2 = (Service) EcoreUtil.create((EClass) ePackage.getEClassifier("TestService2"));
            Admin testAdmin = (Admin) EcoreUtil.create((EClass) ePackage.getEClassifier("TestAdmin"));

            provider.setId("sensor");

            provider.setAdmin(testAdmin);
            provider.eSet(provider.eClass().getEStructuralFeature("testAttribute"), "someAttrib");
            provider.eSet(provider.eClass().getEStructuralFeature("testService1"), testService1);
            provider.eSet(provider.eClass().getEStructuralFeature("testService2"), testService2);

            testService1.eSet(testService1.eClass().getEStructuralFeature("foo"), "foo");
            testService2.eSet(testService2.eClass().getEStructuralFeature("bar"), "bar");

            testAdmin.setFriendlyName(provider.getId());
            testAdmin.eSet(testAdmin.eClass().getEStructuralFeature("testAdmin"), new BigInteger("1000"));

            Provider saved = nexus.save(provider);

            verifyNewProviderNotification(accumulator, saved);
            Metadata resourceMetadata = saved.getAdmin().getMetadata()
                    .get(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME);
            stripMetadata(saved);

            saved.getAdmin().getMetadata().put(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME, resourceMetadata);

            resourceMetadata.setTimestamp(Instant.now());
            saved.getAdmin().setFriendlyName("Something new");

            Provider modified = nexus.save(saved);

            verifyProviderUpdateNotification(accumulator, saved, modified);

        }

        /**
         * @param modified
         */
        private void stripMetadata(Provider provider) {
            provider.eClass().getEAllReferences().stream()
                    .filter(ref -> ProviderPackage.Literals.SERVICE.isSuperTypeOf(ref.getEReferenceType()))
                    .map(provider::eGet).map(Service.class::cast).map(Service::getMetadata).forEach(EMap::clear);
        }

        /**
         * @param accumulator
         * @param provider
         * @param saved
         */
        private void verifyProviderUpdateNotification(NotificationAccumulator accumulator, Provider oldProvider,
                Provider newProvider) {
            EClass eClass = null;
            if (oldProvider == null && newProvider != null) {
                eClass = newProvider.eClass();
                Mockito.verify(accumulator).addProvider(ePackage.getNsURI(), newProvider.eClass().getName(),
                        newProvider.getId());
            } else {
                eClass = oldProvider.eClass();
            }
            eClass.getEAllReferences().stream()
                    .filter(ea -> ea.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                    .filter(ref -> ProviderPackage.Literals.SERVICE.isSuperTypeOf(ref.getEReferenceType()))
                    .forEach(ref -> verifyNotificationsForService(accumulator, oldProvider, newProvider, ref));
            if (oldProvider != null && newProvider == null) {
                Mockito.verify(accumulator).removeProvider(ePackage.getNsURI(), eClass.getName(), oldProvider.getId());
            }
        }

        /**
         * @param accumulator
         * @param oldProvider
         * @param newProvider
         * @param ref
         * @return
         */
        private void verifyNotificationsForService(NotificationAccumulator accumulator, Provider oldProvider,
                Provider newProvider, EReference ref) {
            Service oldService = oldProvider == null ? null : (Service) oldProvider.eGet(ref);
            Service newService = newProvider == null ? null : (Service) newProvider.eGet(ref);
            if (oldService == null && newService != null) {
                verifyNewServiceNottification(accumulator, newProvider, newService, ref.getName());
            } else if (newService == null && oldService != null) {
                verifyServiceRemoveNottification(accumulator, newProvider, oldService, ref.getName());
            } else {
                verifyServiceChangeNottification(accumulator, newProvider, oldService, newService, ref.getName());
            }
        }

    }

    /**
     * @param accumulator
     * @param newProvider
     * @param newService
     * @param string
     */
    private static void verifyServiceChangeNottification(NotificationAccumulator accumulator, Provider provider,
            Service oldService, Service newService, String serviceName) {
        EMFUtil.streamAttributes(oldService.eClass()).filter(oldService::eIsSet)
                .forEach(ea -> verifyServiceAttributeChangeNotification(accumulator, provider, oldService, newService,
                        ea, serviceName));
    }

    /**
     * @param accumulator
     * @param newProvider
     * @param newService
     */
    private static void verifyServiceRemoveNottification(NotificationAccumulator accumulator, Provider provider,
            Service oldService, String serviceName) {
        EMFUtil.streamAttributes(oldService.eClass()).filter(oldService::eIsSet)
                .forEach(ea -> verifyServiceAttributeRemoveNotification(accumulator, provider, oldService, ea));
        Mockito.verify(accumulator).removeService(provider.eClass().getEPackage().getNsURI(),
                EMFUtil.getModelName(provider.eClass()), provider.getId(), serviceName);
    }

    private static void verifyNewProviderNotification(NotificationAccumulator accumulator, Provider provider) {
        Mockito.verify(accumulator).addProvider(provider.eClass().getEPackage().getNsURI(),
                EMFUtil.getModelName(provider.eClass()), provider.getId());
        provider.eClass().getEAllReferences().stream()
                .filter(ea -> ea.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE).filter(provider::eIsSet)
                .map(provider::eGet).filter(Service.class::isInstance).map(Service.class::cast).forEach(
                        s -> verifyNewServiceNottification(accumulator, provider, s, s.eContainingFeature().getName()));
        if (provider instanceof DynamicProvider) {
            ((DynamicProvider) provider).getServices()
                    .forEach(e -> verifyNewServiceNottification(accumulator, provider, e.getValue(), e.getKey()));
        }
    }

    private static void verifyNewServiceNottification(NotificationAccumulator accumulator, Provider provider,
            Service service, String serviceName) {
        Mockito.verify(accumulator).addService(provider.eClass().getEPackage().getNsURI(),
                EMFUtil.getModelName(provider.eClass()), provider.getId(), serviceName);
        EMFUtil.streamAttributes(service.eClass()).filter(service::eIsSet)
                .forEach(ea -> verifyNewServiceAttributeNotification(accumulator, provider, service, serviceName, ea));
    }

    private static void verifyNewServiceAttributeNotification(NotificationAccumulator accumulator, Provider provider,
            Service service, String serviceName, EAttribute attribute) {
        String modelName = EMFUtil.getModelName(provider.eClass());
        Mockito.verify(accumulator).addResource(provider.eClass().getEPackage().getNsURI(), modelName, provider.getId(),
                serviceName, attribute.getName());
        Mockito.verify(accumulator).resourceValueUpdate(provider.eClass().getEPackage().getNsURI(), modelName,
                provider.getId(), serviceName, attribute.getName(), attribute.getEType().getInstanceClass(), null,
                service.eGet(attribute), getTimestampForService(service, attribute));
        Mockito.verify(accumulator).metadataValueUpdate(provider.eClass().getEPackage().getNsURI(), modelName,
                provider.getId(), serviceName, attribute.getName(), null,
                Map.of("value", service.eGet(attribute), "timestamp", getTimestampForService(service, attribute)),
                getTimestampForService(service, attribute));
    }

    private static void verifyServiceAttributeRemoveNotification(NotificationAccumulator accumulator, Provider provider,
            Service oldService, EAttribute attribute) {
        String modelName = EMFUtil.getModelName(provider.eClass());
        Mockito.verify(accumulator).resourceValueUpdate(provider.eClass().getEPackage().getNsURI(), modelName,
                provider.getId(), oldService.eContainingFeature().getName(), attribute.getName(),
                attribute.getEType().getInstanceClass(), oldService.eGet(attribute), null, Mockito.any());
        Mockito.verify(accumulator).metadataValueUpdate(provider.eClass().getEPackage().getNsURI(), modelName,
                provider.getId(), oldService.eContainingFeature().getName(), attribute.getName(),
                Map.of("value", oldService.eGet(attribute), "timestamp", getTimestampForService(oldService, attribute)),
                null, Mockito.any());
        Mockito.verify(accumulator).removeResource(provider.eClass().getEPackage().getNsURI(), modelName,
                provider.getId(), oldService.eContainingFeature().getName(), attribute.getName());
    }

    private static void verifyServiceAttributeChangeNotification(NotificationAccumulator accumulator, Provider provider,
            Service oldService, Service newService, EAttribute attribute, String serviceName) {
        if (Objects.equals(oldService.eGet(attribute), newService.eGet(attribute))) {
            return;
        }
        String modelName = EMFUtil.getModelName(provider.eClass());
        Mockito.verify(accumulator).resourceValueUpdate(eq(provider.eClass().getEPackage().getNsURI()), eq(modelName),
                eq(provider.getId()), eq(serviceName), eq(attribute.getName()),
                eq(attribute.getEType().getInstanceClass()), eq(oldService.eGet(attribute)),
                eq(newService.eGet(attribute)), Mockito.any());
        Mockito.verify(accumulator).metadataValueUpdate(provider.eClass().getEPackage().getNsURI(), modelName,
                provider.getId(), serviceName, attribute.getName(),
                Map.of("value", oldService.eGet(attribute), "timestamp", getTimestampForService(oldService, attribute)),
                Map.of("value", newService.eGet(attribute), "timestamp", getTimestampForService(newService, attribute)),
                getTimestampForService(newService, attribute));
    }

    private static Instant getTimestampForService(Service service, EStructuralFeature feature) {
        return service.getMetadata().get(feature).getTimestamp();
    }

}

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
*   Data In Motion - initial API and implementation
*   Kentyou - fixes and updates to start basic testing
**********************************************************************/
package org.eclipse.sensinact.prototype.model.nexus.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
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
public class NexusTest {

    @Mock
    NotificationAccumulator accumulator;

    private ResourceSet resourceSet;

    @BeforeEach
    void start() {
        resourceSet = EMFTestUtil.createResourceSet();
    }

    @Nested
    public class BasicModelTests {

        @Test
        void basicTest() {

            NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

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
        void basicServiceExtensionTest() {

            NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

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

            NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);

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
    }
}

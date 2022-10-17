/**
 * Copyright (c) 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 *     Kentyou - fixes and updates to start basic testing
 */
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
public class SubscriptionTest {

	@Mock
	NotificationAccumulator accumulator;

	private ResourceSet resourceSet;
	
	@BeforeEach
	void start() {
		resourceSet = new ResourceSetImpl();
		
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("https", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("http", new XMIResourceFactoryImpl());
	
	
		// Register the package to ensure it is available during loading.
		resourceSet.getPackageRegistry().put(SensiNactPackage.eNS_URI, SensiNactPackage.eINSTANCE);
	}
	
	@Nested
	public class BasicModelTests {
		
		@Test
		void basicTest() {
			
			NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
			
			nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test", Instant.now());
			
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
			assertEquals(valueFeature.getEType(),  EcorePackage.Literals.ESTRING);
			
			Object value = service.eGet(valueFeature);
			assertEquals("test", value);
		}

		@Test
		void basicServiceExtensionTest() {
			
			NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
			
			nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test", Instant.now());
			
			Provider provider = nexus.getProvider("TestModel", "testprovider");
			assertNotNull(provider);
			EStructuralFeature serviceFeature = provider.eClass().getEStructuralFeature("testservice");
			Service service = (Service) provider.eGet(serviceFeature);

			nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue2", String.class, "test", Instant.now());
			
			Provider updatedProvider = nexus.getProvider("TestModel", "testprovider");
			assertEquals(provider, updatedProvider);

			Service updateService = (Service) updatedProvider.eGet(serviceFeature);
			assertNotEquals(service, updateService);
			
			EStructuralFeature valueFeature = updateService.eClass().getEStructuralFeature("testValue2");
			
			assertNotNull(valueFeature);
			assertEquals(valueFeature.getEType(),  EcorePackage.Literals.ESTRING);
			
			Object value = updateService.eGet(valueFeature);
			assertEquals("test", value);
		}

		@Test
		void basicSecondServiceTest() {
			
			NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
			
			nexus.handleDataUpdate("TestModel", "testprovider", "testservice", "testValue", String.class, "test", Instant.now());
			
			Provider provider = nexus.getProvider("TestModel", "testprovider");
			assertNotNull(provider);
			nexus.handleDataUpdate("TestModel", "testprovider", "testservice2", "testValue", String.class, "test2", Instant.now());
			Provider updatedProvider = nexus.getProvider("TestModel", "testprovider");
			
			assertNotEquals(provider, updatedProvider);
			
			
			EStructuralFeature serviceFeature = updatedProvider.eClass().getEStructuralFeature("testservice2");
			Service service = (Service) updatedProvider.eGet(serviceFeature);
			
			EStructuralFeature valueFeature = service.eClass().getEStructuralFeature("testValue");
			
			assertNotNull(valueFeature);
			assertEquals(valueFeature.getEType(),  EcorePackage.Literals.ESTRING);
			
			Object value = service.eGet(valueFeature);
			assertEquals("test2", value);
		}
	 }

	@Nested
	public class BasicEventsTest {
		
		private static final String TEST_PROVIDER = "testprovider";
		private static final String TEST_SERVICE = "testservice";
		private static final String TEST_SERVICE_2 = "testservice2";
		private static final String TEST_RESOURCE = "testValue";
		private static final String TEST_RESOURCE_2 = "testValue2";
		private static final String TEST_VALUE = "test";
		private static final String TEST_VALUE_2 = "test2";

		@Test
		void basicTest() {
			
			NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
			
			Instant now = Instant.now();
			nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, String.class, TEST_VALUE, now);
			
			Mockito.verify(accumulator).addProvider(TEST_PROVIDER);
			Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE);
			// TODO - this is missing
//			Mockito.verify(accumulator).addResource(TESTPROVIDER, TESTSERVICE, TEST_RESOURCE);
			Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null, TEST_VALUE, now);
			// TODO - the value is in here, which is surprising, as is the timestamp being a date
			Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null, 
					Map.of("value", TEST_VALUE, "timestamp", new Date(now.toEpochMilli())), now);
			
			Mockito.verifyNoMoreInteractions(accumulator);
		}
		
		@Test
		void basicServiceExtensionTest() {
			
			NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
			
			Instant now = Instant.now();
			Instant before = now.minus(Duration.ofHours(1));
			
			nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, String.class, TEST_VALUE, before);
			nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, "testValue2", String.class, TEST_VALUE, now);
			
			Mockito.verify(accumulator).addProvider(TEST_PROVIDER);
			Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE);
			// TODO - these are missing
//			Mockito.verify(accumulator).addResource(TESTPROVIDER, TESTSERVICE, TEST_RESOURCE);
//			Mockito.verify(accumulator).addResource(TESTPROVIDER, TESTSERVICE, TEST_RESOURCE_2);
			Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null, TEST_VALUE, before);
			Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE_2, null, TEST_VALUE, now);
			
			Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null, 
					Map.of("value", TEST_VALUE, "timestamp", new Date(before.toEpochMilli())), before);
			Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE_2, null, 
					Map.of("value", TEST_VALUE, "timestamp", new Date(now.toEpochMilli())), now);
			
			Mockito.verifyNoMoreInteractions(accumulator);
		}
		
		@Test
		void basicSecondServiceTest() {
			
			NexusImpl nexus = new NexusImpl(resourceSet, SensiNactPackage.eINSTANCE, () -> accumulator);
			
			Instant now = Instant.now();
			Instant before = now.minus(Duration.ofHours(1));
			
			nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, String.class, TEST_VALUE, before);
			nexus.handleDataUpdate("TestModel", TEST_PROVIDER, TEST_SERVICE_2, TEST_RESOURCE, String.class, TEST_VALUE_2, now);
			
			Mockito.verify(accumulator).addProvider(TEST_PROVIDER);
			Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE);
			Mockito.verify(accumulator).addService(TEST_PROVIDER, TEST_SERVICE_2);
			// TODO - these are missing
//			Mockito.verify(accumulator).addResource(TESTPROVIDER, TESTSERVICE, TEST_RESOURCE);
//			Mockito.verify(accumulator).addResource(TESTPROVIDER, TESTSERVICE, TEST_RESOURCE_2);
			Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null, TEST_VALUE, before);
			Mockito.verify(accumulator).resourceValueUpdate(TEST_PROVIDER, TEST_SERVICE_2, TEST_RESOURCE, null, TEST_VALUE_2, now);
			
			Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE, TEST_RESOURCE, null, 
					Map.of("value", TEST_VALUE, "timestamp", new Date(before.toEpochMilli())), before);
			Mockito.verify(accumulator).metadataValueUpdate(TEST_PROVIDER, TEST_SERVICE_2, TEST_RESOURCE, null, 
					Map.of("value", TEST_VALUE_2, "timestamp", new Date(now.toEpochMilli())), now);
			
			Mockito.verifyNoMoreInteractions(accumulator);
		}
	}
}

/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Dictionary;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ModelInstanceBuilder;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Test ResourceFactory
 */
@SuppressWarnings({ "rawtypes", "unused" })
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestResourceIntent<R extends ModelInstance> {
	protected Dictionary<String, Object> props;
	protected AccessTree tree;

	private Mediator mediator;
	private MyModelInstance instance;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void init(@InjectBundleContext BundleContext context) throws InvalidServiceProviderException, InvalidSyntaxException, SecuredAccessException,
			DataStoreException, BundleException {
		mediator = new Mediator(context);
			
			instance = (MyModelInstance) new ModelInstanceBuilder(mediator
					).build("serviceProvider", null, new ModelConfigurationBuilder(
						mediator,ModelConfiguration.class, MyModelInstance.class
						).withStartAtInitializationTime(true).build());
		this.tree = new AccessTreeImpl().withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
	}

	@AfterEach
	public void tearDown() {
		this.instance.unregister();
		this.instance.getRootElement().stop();
	}

	private final AtomicInteger countOn = new AtomicInteger(0);
	private final AtomicInteger countOff = new AtomicInteger(0);
	
	@Test
	public void testOneIntent(@InjectService(timeout = 500) Core core) throws Exception {
		ServiceImpl service1 = instance.getRootElement().addService("testService");
		ResourceImpl r1impl = service1.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");
		ServiceImpl service2 = instance.getRootElement().addService("tostService");
		ResourceImpl r2impl = service2.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");

		Thread.sleep(1000);
		String intentId = core.registerIntent(
			mediator, new Executable<Boolean,Void>(){
				@Override
				public Void execute(Boolean parameter) throws Exception {
					if(parameter.booleanValue()) {
						countOn.incrementAndGet();
					} else {
						countOff.incrementAndGet();
					}
					return null;
				}}, 
			"/serviceProvider/tostService/TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(0, countOff.get());
		ResourceImpl  deleted  = service1.removeResource("TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(0, countOff.get());
		deleted  = service2.removeResource("TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(1, countOff.get());
	}

	@Test
	public void testTwoIntents(@InjectService(timeout = 500) Core core) throws Exception {
		ServiceImpl service1 = instance.getRootElement().addService("testService");
		ResourceImpl r1impl = service1.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");
		ServiceImpl service2 = instance.getRootElement().addService("tostService");
		ResourceImpl r2impl = service2.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");

		Thread.sleep(1000);
		String intentId = core.registerIntent(
			mediator, new Executable<Boolean,Void>(){
				@Override
				public Void execute(Boolean parameter) throws Exception {
					if(parameter.booleanValue()) {
						countOn.incrementAndGet();
					} else {
						countOff.incrementAndGet();
					}
					return null;
				}}, 
			"/serviceProvider/tostService/TestProperty",
			"/serviceProvider/testService/TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(0, countOff.get());
		ResourceImpl  deleted  = service1.removeResource("TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(1, countOff.get());
		deleted  = service2.removeResource("TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(1, countOff.get());
	}
	

	@Test
	public void testTwoIntentsResourcesAppearing(@InjectService(timeout = 500) Core core) throws Exception {
		ServiceImpl service1 = instance.getRootElement().addService("testService");
		ResourceImpl r1impl = service1.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");
		ServiceImpl service2 = instance.getRootElement().addService("tostService");
		
		Thread.sleep(1000);
		String intentId = core.registerIntent(
			mediator, new Executable<Boolean,Void>(){
				@Override
				public Void execute(Boolean parameter) throws Exception {
					if(parameter.booleanValue()) {
						countOn.incrementAndGet();
					} else {
						countOff.incrementAndGet();
					}
					return null;
				}}, 
			"/serviceProvider/tostService/TestProperty",
			"/serviceProvider/testService/TestProperty");
		Thread.sleep(1000);
		assertEquals(0, countOn.get());
		assertEquals(0, countOff.get());		
		ResourceImpl r2impl = service2.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(0, countOff.get());		
		ResourceImpl  deleted  = service1.removeResource("TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(1, countOff.get());
		deleted  = service2.removeResource("TestProperty");
		Thread.sleep(1000);
		assertEquals(1, countOn.get());
		assertEquals(1, countOff.get());
	}


}

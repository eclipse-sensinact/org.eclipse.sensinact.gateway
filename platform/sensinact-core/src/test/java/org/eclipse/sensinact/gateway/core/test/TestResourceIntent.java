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

import static org.junit.Assert.assertEquals;

import java.util.Dictionary;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Test ResourceFactory
 */
@SuppressWarnings({ "rawtypes" })
public class TestResourceIntent<R extends ModelInstance> {
	protected TestContext testContext;
	protected Dictionary<String, Object> props;
	protected AccessTree tree;

	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

	@Before
	public void init() throws Exception {
		this.testContext = new TestContext();
		this.tree = new AccessTreeImpl(testContext.getMediator()).withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
	}

	@After
	public void tearDown() {
		this.testContext.stop();
	}

	private final AtomicInteger countOn = new AtomicInteger(0);
	private final AtomicInteger countOff = new AtomicInteger(0);
	
	@Test
	public void testOneIntent() throws Exception {
		ServiceImpl service1 = this.testContext.getModelInstance().getRootElement().addService("testService");
		ResourceImpl r1impl = service1.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");
		ServiceImpl service2 = this.testContext.getModelInstance().getRootElement().addService("tostService");
		ResourceImpl r2impl = service2.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");

		Thread.sleep(1000);
		String intentId = this.testContext.getSensiNact().registerIntent(
			this.testContext.getMediator(), new Executable<Boolean,Void>(){
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
	public void testTwoIntents() throws Exception {
		ServiceImpl service1 = this.testContext.getModelInstance().getRootElement().addService("testService");
		ResourceImpl r1impl = service1.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");
		ServiceImpl service2 = this.testContext.getModelInstance().getRootElement().addService("tostService");
		ResourceImpl r2impl = service2.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");

		Thread.sleep(1000);
		String intentId = this.testContext.getSensiNact().registerIntent(
			this.testContext.getMediator(), new Executable<Boolean,Void>(){
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
	public void testTwoIntentsResourcesAppearing() throws Exception {
		ServiceImpl service1 = this.testContext.getModelInstance().getRootElement().addService("testService");
		ResourceImpl r1impl = service1.addDataResource(PropertyResource.class, "TestProperty", String.class, "hello");
		ServiceImpl service2 = this.testContext.getModelInstance().getRootElement().addService("tostService");
		
		Thread.sleep(1000);
		String intentId = this.testContext.getSensiNact().registerIntent(
			this.testContext.getMediator(), new Executable<Boolean,Void>(){
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

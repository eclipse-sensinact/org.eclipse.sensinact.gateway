/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.security.signature.test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.framework.FrameworkFactory;
import org.eclipse.sensinact.gateway.security.signature.internal.KeyStoreManagerException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import org.osgi.framework.launch.Framework;

import org.eclipse.sensinact.gateway.security.signature.internal.BundleValidationImpl;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.BundleValidation;

/*
 * signature validation with embedded archive: embedded archives are to be signed by the same signer as the main archive
 * testCheckNOKWithEmbeddedArchive not performed
 */

public class BundleValidationTest
{
	private static final Map<String, String> CONFIGURATION = new HashMap<String, String>();
	static 
	{
		CONFIGURATION.put("felix.cache.rootdir", "./target/felix");
		CONFIGURATION.put("org.osgi.framework.storage", "felix-cache");
		CONFIGURATION.put("felix.auto.deploy.dir", "./target/felix/bundle");
		CONFIGURATION.put("felix.auto.deploy.action", "install,start");
		CONFIGURATION.put("felix.log.level", "4");		
		CONFIGURATION.put("org.osgi.framework.system.packages.extra",
				"org.eclipse.sensinact.gateway.generic.core;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.core.impl;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.core.packet;version=\"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.stream;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.uri;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.parser;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.automata;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.annotation;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.generic.local;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.constraint;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.crypto;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.json;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.mediator;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.properties;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.reflect;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.rest;version= \"1.2.0\"," +
				"org.eclipse.sensinact.gateway.util.xml;version= \"1.2.0\"," +
				"json-20140107.jar;version= \"1.2.0\"," +
				"org.json;version;version= \"1.2.0\"," +
				"org.json.zip;version=\"1.2.0\"");
	}
	
	private Framework felix = new FrameworkFactory().newFramework(CONFIGURATION);
	private Mediator mediator = null;
	private BundleValidation jval = null;
	private Bundle fan = null;
	private Bundle failer = null;
	private Bundle button = null;

	private static final String DEFAULT_KEYSTORE_FILE_PATH = "./src/test/resources/keystore.jks";
	private static final String DEFAULT_KEYSTORE_PASSWORD = "sensiNact_team";
	
	@Before
	public void init() throws
	NoSuchAlgorithmException, KeyStoreManagerException,
	BundleException
	{
		felix = new FrameworkFactory().newFramework(CONFIGURATION);
		felix.init();
		felix.start();
			
		Assert.assertTrue(felix.getState() == Bundle.ACTIVE);
		this.mediator = new Mediator(felix.getBundleContext());
		
		this.jval =  new BundleValidationImpl(this.mediator)
		{
			@Override
		    protected String getKeyStoreFileName()
		    {
		    	return BundleValidationTest.DEFAULT_KEYSTORE_FILE_PATH;
		    }    
			@Override
		    protected String getKeyStorePassword()
		    {
		    	return BundleValidationTest.DEFAULT_KEYSTORE_PASSWORD;
		    }
		    @Override
		    protected String getSignerPassword()
		    {
		    	return BundleValidationTest.DEFAULT_KEYSTORE_PASSWORD;
		    }			
		};
	}
	
	@After
	public void tearDown()
	{
		try
		{			
			felix.stop();
			
			
		} catch (BundleException e) 
		{
			e.printStackTrace();
		}
		this.mediator = null;
		this.jval = null;
		this.fan = null;
		this.failer = null;
		this.button = null;
	}

	@Test
	public void testCheckFanOK() throws BundleException
	{
		this.fan = felix.getBundleContext().installBundle(
				"file:./src/test/resources/fan.jar");

		////logger.log(Level.INFO, "testCheckOK");
		String result = null;
		try
		{
			result = jval.check(this.fan);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}finally
		{
			this.fan.uninstall();
		}
		Assert.assertTrue(result!=null);
	}

	@Test
	public void testCheckFanKO() throws BundleException
	{
		this.failer = felix.getBundleContext().installBundle(
				"file:./src/test/resources/failer-fan.jar");
		
		////logger.log(Level.INFO, "testCheckOK");
		String result = null;
		try
		{
			result = jval.check(this.failer);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}finally
		{
			this.failer.uninstall();
		}
		Assert.assertTrue(result == null);
	}
	
	@Test
	public void testCheckButtonOK() throws BundleException
	{
		this.button = felix.getBundleContext().installBundle(
				"file:./src/test/resources/button.jar");
		
		////logger.log(Level.INFO, "testCheckOK");
		String result = null;
		try
		{
			result = jval.check(this.button);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		} finally
		{
			this.button.uninstall();
		}
		Assert.assertTrue(result!=null);
	}

}

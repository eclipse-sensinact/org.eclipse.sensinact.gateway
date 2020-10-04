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

package org.eclipse.sensinact.gateway.core.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestSecurityPattern extends MidOSGiTest {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	private static final String SLIDERS_DEFAULT = "[\"slider01\",\"slider02\",\"slider11\"]";
	private static final String SLIDERS_PROP = "org.eclipse.sensinact.simulated.sliders";
	private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	Method getDescription = null;
	Method getMethod = null;
	Method setMethod = null;
	Method actMethod = null;

	public TestSecurityPattern() throws Exception {
		super();
		getDescription = Describable.class.getDeclaredMethod("getDescription");
		getMethod = Resource.class.getDeclaredMethod("get", new Class<?>[] { String.class });
		setMethod = Resource.class.getDeclaredMethod("set", new Class<?>[] { String.class, Object.class });
		actMethod = ActionResource.class.getDeclaredMethod("act", new Class<?>[] { Object[].class });
	}

	/**
	 * @inheritDoc
	 *
	 * @see MidOSGiTest#isExcluded(java.lang.String)
	 */
	public boolean isExcluded(String fileName) {
		if ("org.apache.felix.framework.security.jar".equals(fileName)) {
			return true;
		}
		return false;
	}

	/**
	 * @inheritDoc
	 *
	 * @see MidOSGiTest#doInit(java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doInit(Map configuration) {
		configuration.put("org.osgi.framework.system.packages.extra",
				"org.eclipse.sensinact.gateway.test," + "com.sun.net.httpserver," + "javax.net.ssl,"
						+ "javax.xml.parsers," + "javax.imageio," + "javax.management," + "javax.naming," + "javax.sql,"
						+ "javax.swing," + "javax.swing.border," + "javax.swing.event," + "javax.mail,"
						+ "javax.mail.internet," + "javax.management.modelmbean," + "javax.management.remote,"
						+ "javax.xml.parsers," + "javax.security.auth," + "javax.security.cert," + "junit.framework,"
						+ "junit.textui," + "org.w3c.dom," + "org.xml.sax," + "org.xml.sax.helpers," + "sun.misc,"
						+ "sun.security.action");

		configuration.put(GUI_ENABLED, "false");
		configuration.put(SLIDERS_PROP, SLIDERS_DEFAULT);

		configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
		configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");

		configuration.put("org.eclipse.sensinact.gateway.security.database",
				new File("src/test/resources/sensinact.sqlite").getAbsolutePath());

		configuration.put("felix.auto.start.1",
				"file:target/felix/bundle/org.osgi.compendium.jar "
						+ "file:target/felix/bundle/org.apache.felix.framework.security.jar "
						+ "file:target/felix/bundle/org.apache.felix.configadmin.jar "
						+ "file:target/felix/bundle/org.apache.felix.fileinstall.jar");

		configuration.put("felix.auto.install.2",
				"file:target/felix/bundle/sensinact-utils.jar "
						+ "file:target/felix/bundle/sensinact-datastore-api.jar "
						+ "file:target/felix/bundle/sensinact-sqlite-connector.jar "
						+ "file:target/felix/bundle/sensinact-common.jar "
						+ "file:target/felix/bundle/sensinact-framework-extension.jar "
						+ "file:target/felix/bundle/dynamicBundle.jar");

		configuration.put("felix.auto.start.2", "file:target/felix/bundle/sensinact-test-configuration.jar "
				+ "file:target/felix/bundle/sensinact-signature-validator.jar ");

		configuration.put("felix.auto.start.3",
				"file:target/felix/bundle/sensinact-core.jar " + "file:target/felix/bundle/sensinact-generic.jar ");

		configuration.put("felix.auto.start.4", "file:target/felix/bundle/slider.jar ");

		configuration.put("felix.log.level", "4");
	}

	@Ignore
	@Test
	public void testSecurityAccessWithPattern() throws Throwable {
		// slider[0-9]{2} - authenticated access level
		// slider[0-9]{2}/admin - admin authenticated access level
		// cea user is admin on slider[0-9]{2}

		// slider0[0-9] - authenticated access level
		// slider0[0-9]/cursor - authenticated access level
		// fake user is authenticated on slider0[0-9]

		// slider1[0-9] - authenticated access level
		// slider1[0-9]/cursor - authenticated access level
		// fake2 user is authenticated on slider1[0-9]

		MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);

		Core core = mid.buildProxy();
		Session session = core.getAnonymousSession();
		assertNotNull(session);

		Set providers = session.serviceProviders();
		System.out.println("====================================>>>>>");
		System.out.println(providers);
		System.out.println("====================================>>>>>");
		assertTrue(providers.isEmpty());

		// ******************************************************
		// admin
		// the admin user is suppose to see every thing
		// service providers and services
		MidProxy<Authentication> midCredentials = new MidProxy<Authentication>(classloader, this, Authentication.class);

		midCredentials.buildProxy(Credentials.class.getCanonicalName(), new Class<?>[] { String.class, String.class },
				new Object[] { "cea", "sensiNact_team" });

		Method method = mid.getContextualizedType().getDeclaredMethod("getSession",
				new Class<?>[] { midCredentials.getContextualizedType() });

		session = (Session) mid.toOSGi(method, new Object[] { midCredentials.getInstance() });

		assertNotNull(session);

		providers = session.serviceProviders();
		assertEquals(3, providers.size());
		Iterator<ServiceProvider> iterator = providers.iterator();

		while (iterator.hasNext()) {
			MidProxy<ServiceProvider> provider = new MidProxy<ServiceProvider>(classloader, this,
					ServiceProvider.class);

			ServiceProvider serviceProvider = provider.buildProxy(iterator.next());
			assertEquals(2, serviceProvider.getServices().size());
			System.out.println(serviceProvider.getDescription().getJSON());
		}

		// *************************************
		// fake
		// the fake user is suppose to see only two service providers
		// and only the cursor service for each one
		midCredentials = new MidProxy<Authentication>(classloader, this, Authentication.class);

		midCredentials.buildProxy(Credentials.class.getCanonicalName(), new Class<?>[] { String.class, String.class },
				new Object[] { "fake", "fake" });

		method = mid.getContextualizedType().getDeclaredMethod("getSession",
				new Class<?>[] { midCredentials.getContextualizedType() });

		session = (Session) mid.toOSGi(method, new Object[] { midCredentials.getInstance() });

		assertNotNull(session);

		providers = session.serviceProviders();

		assertEquals(2, providers.size());
		iterator = providers.iterator();

		while (iterator.hasNext()) {
			MidProxy<ServiceProvider> provider = new MidProxy<ServiceProvider>(classloader, this,
					ServiceProvider.class);

			ServiceProvider serviceProvider = provider.buildProxy(iterator.next());
			assertEquals(1, serviceProvider.getServices().size());
			System.out.println(serviceProvider.getDescription().getJSON());
		}

		// ***************************************
		// fake2
		// the fake2 user is suppose to see only one service provider
		// and only its cursor service
		midCredentials = new MidProxy<Authentication>(classloader, this, Authentication.class);

		midCredentials.buildProxy(Credentials.class.getCanonicalName(), new Class<?>[] { String.class, String.class },
				new Object[] { "fake2", "fake2" });

		method = mid.getContextualizedType().getDeclaredMethod("getSession",
				new Class<?>[] { midCredentials.getContextualizedType() });

		session = (Session) mid.toOSGi(method, new Object[] { midCredentials.getInstance() });

		assertNotNull(session);

		providers = session.serviceProviders();
		assertEquals(1, providers.size());
		iterator = providers.iterator();

		while (iterator.hasNext()) {
			MidProxy<ServiceProvider> provider = new MidProxy<ServiceProvider>(classloader, this,
					ServiceProvider.class);

			ServiceProvider serviceProvider = provider.buildProxy(iterator.next());
			assertEquals(1, serviceProvider.getServices().size());
			System.out.println(serviceProvider.getDescription().getJSON());
		}
	}
}

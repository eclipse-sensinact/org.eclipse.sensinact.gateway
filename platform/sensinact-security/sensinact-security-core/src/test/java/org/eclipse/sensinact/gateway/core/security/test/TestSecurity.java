/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.core.security.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class TestSecurity extends AbstractConfiguredSecurityTest {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	@Disabled
	@Test
	@WithConfiguration(
			pid = "SQLiteDataStoreService",
			location = "?",
			properties = {
					@Property(key = "database", value = "${sqlitedb}")
			}
		)
	public void testSecurityAccessInitialization(@InjectService(timeout = 1000) Core core,
			@InjectBundleContext BundleContext context) throws Throwable {
		Session session = core.getAnonymousSession();
		assertNotNull(session);

		// Restart the bundles due to the unpleasant startup ordering (they create models before
		// security is started)
		Arrays.stream(context.getBundles())
			.filter(b -> b.getSymbolicName().startsWith("org.eclipse.sensinact.gateway.simulated.devices"))
			.forEach(b -> {
				try {
					b.stop();
					b.start();
				} catch (BundleException be) {}
			});
		
		Set<ServiceProvider> providers = session.serviceProviders();
		System.out.println("====================================>>>>>");
		System.out.println(providers);
		System.out.println("====================================>>>>>");
		assertEquals(3, providers.size());
		
		Resource resource = session.resource("fan", "admin", "location");
		GetResponse getResponse = resource.get(DataResource.VALUE);
		assertEquals(200, getResponse.getStatusCode());
		
		SetResponse setResponse = resource.set(DataResource.VALUE, "24:68");
		assertEquals(403, setResponse.getStatusCode());

		Credentials credentials = new Credentials("cea", "sensiNact_team");
		session = core.getSession(credentials);
		
		assertNotNull(session);

		providers = session.serviceProviders();
		assertEquals(3, providers.size());
		Iterator<ServiceProvider> iterator = providers.iterator();
		
		while (iterator.hasNext()) {
			ServiceProvider serviceProvider = iterator.next();

			System.out.println(serviceProvider.getDescription().getJSON());
		}
//		
//		MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
//
//		Core core = mid.buildProxy();
//		Session session = core.getAnonymousSession();
//		assertNotNull(session);
//
//		Set providers = session.serviceProviders();
//		Iterator iterator = providers.iterator();
//
//		while (iterator.hasNext()) {
//			MidProxy<ServiceProvider> provider = new MidProxy<ServiceProvider>(classloader, this,
//					ServiceProvider.class);
//
//			ServiceProvider serviceProvider = provider.buildProxy(iterator.next());
//
//			System.out.println(serviceProvider.getDescription().getJSON());
//		}
//		System.out.println("============================================");
//
//		MidProxy<Authentication> midCredentials = new MidProxy<Authentication>(classloader, this, Authentication.class);
//
//		midCredentials.buildProxy(Credentials.class.getCanonicalName(), new Class<?>[] { String.class, String.class },
//				new Object[] { "cea", "sensiNact_team" });
//
//		Method method = mid.getContextualizedType().getDeclaredMethod("getSession",
//				new Class<?>[] { midCredentials.getContextualizedType() });
//
//		session = (Session) mid.toOSGi(method, new Object[] { midCredentials.getInstance() });
//
//		assertNotNull(session);
//
//		providers = session.serviceProviders();
//		iterator = providers.iterator();
//
//		while (iterator.hasNext()) {
//			MidProxy<ServiceProvider> provider = new MidProxy<ServiceProvider>(classloader, this,
//					ServiceProvider.class);
//
//			ServiceProvider serviceProvider = provider.buildProxy(iterator.next());
//
//			System.out.println(serviceProvider.getDescription().getJSON());
//		}

	}

}

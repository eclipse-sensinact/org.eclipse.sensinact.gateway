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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@Disabled
public class TestSecurity {
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

	@Test
	@Disabled
	public void testSecurityAccessInitialization(@InjectService Core core) throws Throwable {
		Session session = core.getAnonymousSession();
		assertNotNull(session);

		
		Set providers = session.serviceProviders();
		System.out.println("====================================>>>>>");
		System.out.println(providers);
		System.out.println("====================================>>>>>");
		assertTrue(providers.isEmpty());
//
		Credentials credentials = new Credentials("cea", "sensiNact_team");
		session = core.getSession(credentials);
		
		assertNotNull(session);
//
		providers = session.serviceProviders();
		assertEquals(3, providers.size());
		Iterator<ServiceProvider> iterator = providers.iterator();
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

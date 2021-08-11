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
package org.eclipse.sensinact.gateway.core.security.impl;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.core.security.UserManagerFactory;

/**
 *
 */
public class UserManagerFactoryImpl implements UserManagerFactory<UserManagerImpl> {
	
	@Override
	public Class<UserManagerImpl> getType() {
		return UserManagerImpl.class;
	}

	@Override
	public void newInstance(Mediator mediator) throws SecuredAccessException {
		UserManager manager = new UserManagerImpl(mediator);
		mediator.register(new Hashtable(){{this.put("identityMaterial",Credentials.class.getCanonicalName());}}, 
			manager, new Class<?>[] { UserManager.class, AuthenticationService.class });
	}
}

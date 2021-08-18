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
package org.eclipse.sensinact.gateway.core.security.user;

import java.util.Hashtable;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilderFactory;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

/**
 * {@link UserKeyBuilderFactory} service implementation
 */
public class UserKeyBuilderFactoryImpl implements UserKeyBuilderFactory<Credentials,Credentials,UserKeyBuilderImpl> {
	
	@Override
	public Class<UserKeyBuilderImpl> getType() {
		return UserKeyBuilderImpl.class;
	}

	@Override
	public void newInstance(Mediator mediator) throws SecuredAccessException {
		UserKeyBuilder<Credentials,Credentials> builder = new UserKeyBuilderImpl(mediator);
		mediator.register(new Hashtable() {{
			this.put("identityMaterial",Credentials.class.getCanonicalName());
		}}, builder, new Class<?>[] { UserKeyBuilder.class });
	}
}

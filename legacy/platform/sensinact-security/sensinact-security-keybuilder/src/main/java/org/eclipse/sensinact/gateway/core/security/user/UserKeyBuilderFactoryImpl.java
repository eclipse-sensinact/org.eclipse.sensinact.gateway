/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.user;

import java.util.Hashtable;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilderFactory;

import aQute.bnd.annotation.Resolution;
import aQute.bnd.annotation.spi.ServiceProvider;

import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

/**
 * {@link UserKeyBuilderFactory} service implementation
 */
@ServiceProvider(value = UserKeyBuilderFactory.class, resolution = Resolution.OPTIONAL)
public class UserKeyBuilderFactoryImpl implements UserKeyBuilderFactory<Credentials,Credentials,UserKeyBuilderImpl> {
	
	@Override
	public Class<UserKeyBuilderImpl> getType() {
		return UserKeyBuilderImpl.class;
	}

	@Override
	public void newInstance(Mediator mediator) throws SecuredAccessException {
		UserKeyBuilder<Credentials,Credentials> builder = new UserKeyBuilderImpl(mediator);
		mediator.register(new Hashtable<String,Object>() {
			private static final long serialVersionUID = 1L;
		{
			this.put("identityMaterial",Credentials.class.getCanonicalName());
		}}, builder, new Class<?>[] { UserKeyBuilder.class });
	}
}

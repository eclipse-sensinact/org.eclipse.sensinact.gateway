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
package org.eclipse.sensinact.gateway.core.security.user.impl;

import java.security.InvalidKeyException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.User;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.CryptoUtils;

/**
 *  {@link UserKeyBuilder} implementation in charge of building {@link UserKey}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UserKeyBuilderImpl implements UserKeyBuilder<Credentials,Credentials> {
	
	private Mediator mediator;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link UserKeyBuilder}	to be instantiated to
	 * interat with the OSGi host environement 
	 */
	public UserKeyBuilderImpl(Mediator mediator)  {
		this.mediator = mediator;
	}

	@Override
	public UserKey buildKey(Credentials credentials) throws InvalidKeyException, InvalidCredentialException, DataStoreException {
		if(Credentials.ANONYMOUS_LOGIN.equals(credentials.login) && Credentials.ANONYMOUS_PASSWORD.equals(credentials.password))
			return null;		
		final String login = credentials.login;
		final String md5 = CryptoUtils.cryptWithMD5(credentials.password);
		User user = this.mediator.callService(UserManager.class, new Executable<UserManager,User>(){
			@Override
			public User execute(UserManager manager) throws Exception {
				return manager.getUser(login, md5);
			}				
		});
		if (user == null) 
			return null;
		else
			return new UserKey(user.getPublicKey());
	}

}

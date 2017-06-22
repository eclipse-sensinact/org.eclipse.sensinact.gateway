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
package org.eclipse.sensinact.gateway.core.security.impl;

import java.security.InvalidKeyException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.util.CryptoUtils;

public class AuthenticationServiceImpl implements AuthenticationService
{
	private UserDAO userDAO;
	private Mediator mediator;

	public AuthenticationServiceImpl(Mediator mediator) throws DAOException
	{
		this.mediator = mediator;
		this.userDAO = new UserDAO(mediator);
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see AuthenticationService#
	 * getUserId(Credentials)
	 */
	@Override
	public UserKey buildKey(Credentials credentials)
	{
		Session.Key key = new Session.Key();
		try 
		{
			String md5 = CryptoUtils.cryptWithMD5(credentials.password);
			
//			System.out.println("---------------------------");
//			System.out.println(credentials.password + "==" + md5);
//			System.out.println("---------------------------");
			
			UserEntity userEntity = this.userDAO.find(
					credentials.login, md5);
			
			key.setUid(userEntity.getIdentifier());
			key.setPublicKey(userEntity.getPublicKey());
			
			
		} catch (DAOException e) 
		{
			mediator.error(e);
		}
		catch (InvalidKeyException e)
		{
			mediator.error(e);
		}
		return key;
	}

}

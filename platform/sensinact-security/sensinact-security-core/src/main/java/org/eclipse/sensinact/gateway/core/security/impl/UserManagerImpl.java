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
import java.util.HashMap;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService;
import org.eclipse.sensinact.gateway.core.security.User;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.core.security.UserUpdater;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UserManagerImpl implements UserManager, AuthenticationService {
	private Mediator mediator;
	private UserDAO userDAO;
	private UserEntity anonymous;

	/**
	 * 
	 * @param mediator
	 * @throws DataStoreException
	 * @throws DAOException
	 * 
	 */
	public UserManagerImpl(Mediator mediator) throws SecuredAccessException {
		this.mediator = mediator;
		try {
			ServiceReference<SecurityDataStoreService> reference = this.mediator.getContext()
					.getServiceReference(SecurityDataStoreService.class);

			this.userDAO = new UserDAO(mediator, this.mediator.getContext().getService(reference));

			anonymous = userDAO.find(ANONYMOUS_ID);
		} catch (DataStoreException | NullPointerException | IllegalArgumentException e) {
			mediator.error(e);
			throw new SecuredAccessException(e);
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see java.lang.Object#finalize()
	 */
	public void finalize() {
		this.userDAO = null;

		try {
			ServiceReference<SecurityDataStoreService> reference = this.mediator.getContext()
					.getServiceReference(SecurityDataStoreService.class);

			this.mediator.getContext().ungetService(reference);
		} catch (NullPointerException | IllegalArgumentException e) {
			mediator.error(e);
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserManager#
	 *      loginExists(java.lang.String)
	 */
	@Override
	public boolean loginExists(final String login) throws SecuredAccessException, DataStoreException {
		return this.userDAO.select(new HashMap<String, Object>() {
			{
				this.put("UNAME", login);
			}
		}) != null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserManager#
	 *      accountExists(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean accountExists(String account) throws SecuredAccessException, DataStoreException {
		return this.userDAO.findFromAccount(account) != null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserManager#
	 *      createUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public UserUpdater createUser(String login, String password, String account) throws SecuredAccessException {
		return null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserManager#
	 *      getUser(java.lang.String, java.lang.String)
	 */
	@Override
	public User getUser(String login, String password) throws SecuredAccessException, DataStoreException {
		return userDAO.find(login, password);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserManager#
	 *      updateUserPassword(java.lang.String, java.lang.String)
	 */
	@Override
	public UserUpdater updateUserPassword(String account, String newPassword) throws SecuredAccessException {
		return null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserManager#
	 *      getUserFromPublicKey(java.lang.String)
	 */
	@Override
	public User getUserFromPublicKey(String publicKey) throws SecuredAccessException, DataStoreException {
		if (publicKey == null) {
			return anonymous;
		}
		return userDAO.find(publicKey);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserManager#
	 *      getUserFromAccount(java.lang.String)
	 */
	@Override
	public User getUserFromAccount(String account) throws SecuredAccessException, DataStoreException {
		if (account == null) {
			return anonymous;
		}
		return userDAO.findFromAccount(account);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see AuthenticationService#buildKey(Credentials)
	 */
	@Override
	public UserKey buildKey(Credentials credentials)
			throws InvalidKeyException, DAOException, InvalidCredentialException, DataStoreException {
		String md5 = CryptoUtils.cryptWithMD5(credentials.password);
		UserEntity userEntity = this.userDAO.find(credentials.login, md5);

		if (userEntity == null) {
			return null;
		} else {
			return new UserKey(userEntity.getPublicKey());
		}
	}

}

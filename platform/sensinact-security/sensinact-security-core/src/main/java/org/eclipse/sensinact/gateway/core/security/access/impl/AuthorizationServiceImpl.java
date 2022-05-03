/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.access.impl;

import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationServiceException;
import org.eclipse.sensinact.gateway.core.security.dao.AuthenticatedAccessLevelDAO;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.entity.AuthenticatedAccessLevelEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AuthorizationServiceImpl implements AuthorizationService {
	private AuthenticatedAccessLevelDAO authenticatedAccessLevelDAO;

	/**
	 * Constructor
	 * 
	 * @param authenticatedAccessLevelDAO
	 * 
	 * @throws DAOException
	 */
	public AuthorizationServiceImpl(AuthenticatedAccessLevelDAO authenticatedAccessLevelDAO)
			throws DAOException {
		this.authenticatedAccessLevelDAO = authenticatedAccessLevelDAO;
	}

	@Override
	public AccessLevelOption getAuthenticatedAccessLevelOption(String path, long uid)
			throws AuthorizationServiceException {
		if (uid <= 0) 
			return AccessLevelOption.ANONYMOUS;
		try {
			AuthenticatedAccessLevelEntity userAccessLevelEntity = this.authenticatedAccessLevelDAO.find(path, uid);
			return AccessLevelOption.valueOf(userAccessLevelEntity);
		} catch (DAOException | DataStoreException e) {
			throw new AuthorizationServiceException(e);
		}
	}

	@Override
	public AccessLevelOption getAuthenticatedAccessLevelOption(String path, String publicKey)
			throws AuthorizationServiceException {
		if (publicKey == null || publicKey.startsWith("anonymous")) 
			return AccessLevelOption.ANONYMOUS;
		try {
			AuthenticatedAccessLevelEntity userAccessLevelEntity = this.authenticatedAccessLevelDAO.find(path,
					publicKey);
			return AccessLevelOption.valueOf(userAccessLevelEntity);
		} catch (Exception e) {
			throw new AuthorizationServiceException(e);
		}
	}
}

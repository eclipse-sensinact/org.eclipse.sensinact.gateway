/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security;

/**
 * Provides the set of authorized access methods for a specific user and a
 * specific resource
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AuthorizationService {
	/**
	 * Returns the {@link AccessLevelOption} for the user whose long identifier is
	 * passed as parameter and for the targeted resource whose path is also passed
	 * as parameter
	 * 
	 * @param path
	 *            the string path of the targeted resource *
	 * @param uid
	 *            the long identifier of the user
	 * 
	 * @return the {@link AccessLevelOption} for the specified user and resource
	 * @throws DAOException
	 */
	AccessLevelOption getAuthenticatedAccessLevelOption(String path, long uid) throws AuthorizationServiceException;

	/**
	 * Returns the {@link AccessLevelOption} for the user whose String public key is
	 * passed as parameter and for the targeted resource whose path is also passed
	 * as parameter
	 * 
	 * @param path
	 *            the string path of the targeted resource *
	 * @param publicKey
	 *            the String public key of the user
	 * 
	 * @return the {@link AccessLevelOption} for the specified user and resource
	 */
	AccessLevelOption getAuthenticatedAccessLevelOption(String path, String publicKey)
			throws AuthorizationServiceException;
}

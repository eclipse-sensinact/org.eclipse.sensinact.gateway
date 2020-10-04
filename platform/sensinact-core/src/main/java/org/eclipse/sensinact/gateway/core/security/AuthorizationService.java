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

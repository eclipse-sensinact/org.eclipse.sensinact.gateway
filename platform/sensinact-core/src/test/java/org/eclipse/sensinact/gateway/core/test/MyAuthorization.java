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
package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationServiceException;

class MyAuthorization<R> implements AuthorizationService {
	/**
	 * @inheritDoc
	 *
	 * @see AuthorizationService#getAuthenticatedAccessLevelOption(java.lang.String,
	 *      long)
	 */
	@Override
	public AccessLevelOption getAuthenticatedAccessLevelOption(String path, long uid)
			throws AuthorizationServiceException {
		return AccessLevelOption.ANONYMOUS;
	}

	/**
	 * @inheritDoc
	 *
	 * @see AuthorizationService#getAuthenticatedAccessLevelOption(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public AccessLevelOption getAuthenticatedAccessLevelOption(String path, String publicKey)
			throws AuthorizationServiceException {
		return AccessLevelOption.ANONYMOUS;
	}
}
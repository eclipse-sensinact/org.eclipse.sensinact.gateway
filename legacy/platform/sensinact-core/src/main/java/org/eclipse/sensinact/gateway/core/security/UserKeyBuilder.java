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
 * Authentication service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface UserKeyBuilder<C,A extends Authentication<C>> {
	/**
	 * Build a {@link UserKey} for the user whose the {@link Credentials} are passed
	 * as parameter
	 * 
	 * @param authentication the {@link Authentication} from which to build a {@link UserKey}
	 * 
	 * @return a {@link UserKey} for the specified {@link Credentials}
	 * 
	 * @throws Exception if the {@link UserKey} build process is in error 
	 */
	UserKey buildKey(A authentication) throws Exception;
}

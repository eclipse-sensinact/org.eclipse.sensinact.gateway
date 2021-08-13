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

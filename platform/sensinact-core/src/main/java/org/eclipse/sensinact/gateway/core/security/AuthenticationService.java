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
package org.eclipse.sensinact.gateway.core.security;

/**
 * Authentication service
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface AuthenticationService {
	/**
	 * Build a {@link UserKey} for the user whose the {@link Credentials} are passed
	 * as parameter
	 * 
	 * @param credentials
	 *            the {@link Credentials} from which to build a {@link UserKey}
	 * 
	 * @return a {@link UserKey} for the specified {@link Credentials}
	 * 
	 * @throws Exception
	 */
	UserKey buildKey(Credentials credentials) throws Exception;
}

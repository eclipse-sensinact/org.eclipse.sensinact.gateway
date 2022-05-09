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

import org.eclipse.sensinact.gateway.core.Session;

/**
 * {@link Authentication} implementation holding an existing sensiNact's {@link Session} authentication material
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SessionToken implements Authentication<String> {
	private String sessionToken;

	/**
	 * Constructor
	 * 
	 * @param sessionToken the String {@link Session}'s authentication material held
	 * by the SessionToken to be instantiated
	 */
	public SessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
	}

	@Override
	public String getAuthenticationMaterial() {
		return this.sessionToken;
	}
}
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
 * {@link Authentication} implementation holding third party authentication material
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessToken implements Authentication<String> {
	public static final String HEADER_SEP = " ";
	
	private String accessToken;
	
	/**
	 * Constructor
	 * 
	 * @param accessToken the String authentication material held
	 * by the AccessToken to be instantiated
	 */
	public AccessToken(String accessToken) {
		if(accessToken == null) {
			this.accessToken = null;
			return;
		}
		String[] accessTokenParts = accessToken.split(HEADER_SEP);
		int index = accessTokenParts.length == 1 ? 0 : 1;
		this.accessToken = accessTokenParts[index];
	}

	@Override
	public String getAuthenticationMaterial() {
		return this.accessToken;
	}
}
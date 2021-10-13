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

import java.util.Base64;

public class Credentials implements Authentication<Credentials> {
	public static final String HEADER_SEP = " ";
	public static final String BASE64_SEP = ":";

	public static final String ANONYMOUS_LOGIN = "anonymous";
	public static final String ANONYMOUS_PASSWORD = "anonymous";

	public final String login;
	public final String password;

	public Credentials(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public Credentials(String encoded) {
		String localLogin = null;
		String localPassword = null;
		try {
			String[] encodedElements = encoded.split(HEADER_SEP);
			int index = encodedElements.length == 1 ? 0 : 1;
			byte[] credBytes = Base64.getDecoder().decode(encodedElements[index]);
			String[] credentials = new String(credBytes).split(":");

			localLogin = credentials[0];
			localPassword = credentials[1];

		} catch (Exception e) {
			localLogin = ANONYMOUS_LOGIN;
			localPassword = ANONYMOUS_PASSWORD;
		}
		this.login = localLogin;
		this.password = localPassword;
	}

	@Override
	public Credentials getAuthenticationMaterial() {
		return this;
	}

}
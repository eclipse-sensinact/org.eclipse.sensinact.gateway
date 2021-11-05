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
package org.eclipse.sensinact.gateway.security.oauth2;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.json.JSONObject;

public interface OAuthServer {

	public static final String AUTH_BASEURL_PROP = "org.eclipse.sensinact.gateway.auth.server.baseurl";
	
	public static final String AUTH_BASEURL_DEFAULT = "/sensinact.auth";
	
	JSONObject verify(String code, ServletRequest request);

	boolean handleSecurity(ServletRequest request, ServletResponse response);

	String basicToken(ServletRequest request, String token);

	UserInfo check(String access_token) throws IOException;

	UserInfo anonymous();

	void addCredentials(String access_token, UserInfo newUser);
}

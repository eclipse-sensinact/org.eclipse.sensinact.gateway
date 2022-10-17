/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.oauth2;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jakarta.json.JsonObject;


public interface OAuthServer {

	public static final String AUTH_BASEURL_PROP = "org.eclipse.sensinact.gateway.auth.server.baseurl";
	
	public static final String AUTH_BASEURL_DEFAULT = "/sensinact.auth";
	
	JsonObject verify(String code, ServletRequest request);

	boolean handleSecurity(ServletRequest request, ServletResponse response);

	String basicToken(ServletRequest request, String token);

	UserInfo check(String access_token) throws IOException;

	UserInfo anonymous();

	void addCredentials(String access_token, UserInfo newUser);
}

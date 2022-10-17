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

import java.net.URI;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

public interface IdentityServer {

	int register(String role, String action, URI resource);

	int register(String role, String action, Pattern resource);

	void unregister(int id);

	boolean check(UserInfo user, ServletRequest request);

	UserInfo getUserInfo(String token, String authorization);
}

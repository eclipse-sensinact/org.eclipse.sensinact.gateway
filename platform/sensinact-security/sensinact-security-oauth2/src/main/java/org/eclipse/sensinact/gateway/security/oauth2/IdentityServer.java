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

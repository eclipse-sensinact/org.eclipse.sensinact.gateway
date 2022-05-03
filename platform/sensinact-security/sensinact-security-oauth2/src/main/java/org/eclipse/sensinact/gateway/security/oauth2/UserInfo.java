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

import java.util.ArrayList;
import java.util.List;

public interface UserInfo {
	
	final static public List<String> defaultRoles = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("anonymous");
			add("users");
			add("productors");
			add("admins");
			add("admin");
		}
	};

	String token();

	boolean hasRole(String role);

	List<String> roles();

	boolean check(String token);

	void dispose();

	boolean expire();

	Object get(String field);
}

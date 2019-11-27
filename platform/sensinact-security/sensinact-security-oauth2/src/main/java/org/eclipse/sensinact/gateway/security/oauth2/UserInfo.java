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

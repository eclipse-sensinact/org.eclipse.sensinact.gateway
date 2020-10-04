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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

abstract class IdentityServerWrapper implements IdentityServer {
	static Hashtable<String, List<Pattern>> describe = new Hashtable<String, List<Pattern>>();
	static Hashtable<String, List<Pattern>> read = new Hashtable<String, List<Pattern>>();
	static Hashtable<String, List<Pattern>> write = new Hashtable<String, List<Pattern>>();
	static Hashtable<String, List<Pattern>> create = new Hashtable<String, List<Pattern>>();
	static Hashtable<String, List<Pattern>> get = new Hashtable<String, List<Pattern>>();
	static Hashtable<String, List<Pattern>> post = new Hashtable<String, List<Pattern>>();
	static Hashtable<String, List<Pattern>> put = new Hashtable<String, List<Pattern>>();
	static Hashtable<String, List<Pattern>> delete = new Hashtable<String, List<Pattern>>();
	final static Hashtable<String, Hashtable<String, List<Pattern>>> actions = new Hashtable<String, Hashtable<String, List<Pattern>>>() {
		private static final long serialVersionUID = 1L;
		{
			put("DESCRIBE", describe);
			put("READ", read);
			put("WRITE", write);
			put("CREATE", create);
			put("GET", get);
			put("POST", post);
			put("PUT", put);
			put("DELETE", delete);
		}
	};

	@Override
	public int register(String role, String action, Pattern pattern) {
		Hashtable<String, List<Pattern>> table = actions.get(action);
		if (table != null) {
			List<Pattern> list = table.get(role);
			if (list == null) {
				list = new ArrayList<Pattern>();
				table.put(role, list);
			}
			list.add(pattern);
		}
		return 0;
	}

	public int register(String role, String action, URI resource) {
		Hashtable<String, List<Pattern>> table = actions.get(action);
		if (table != null) {
			Pattern pattern = Pattern.compile(resource.toString());
			List<Pattern> list = table.get(role);
			if (list == null) {
				list = new ArrayList<Pattern>();
				table.put(role, list);
			}
			list.add(pattern);
		}
		return 0;
	}

	public void unregister(int id) {
	}

	private boolean checkPattern(UserInfo user, List<Pattern> search, String needle) {
		if(search == null || search.isEmpty()) {
			return  false;
		}
		Iterator<Pattern> it = search.iterator();
		while (it.hasNext()) {
			Matcher matcher = it.next().matcher(needle);
			while (matcher.find()) {
				boolean result = false;
				try {
					String userreg = matcher.group("user");
					String groupreg = matcher.group("group");
					String rolereg = matcher.group("role");
					if (userreg != null) {
						if (userreg.equals(user.get("name")))
							result = true;
					} else if (groupreg != null) {
						if (groupreg.equals(user.get("group")))
							result = true;
					} else if (rolereg != null) {

						if (user.roles().contains(rolereg))
							result = true;
					} else
						result = true;
					if (result == true)
						return true;
				} catch (Exception e) {
					/**
					 * exception occurs when the pattern matches the URL but doesn't contain
					 * matcher.group In this case the first interpretation as a good pattern is
					 * enough.
					 */
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean check(UserInfo user, ServletRequest request) {
		if (user == null)
			return false;
		//check admin role to allow all URL
		if (user.roles().contains(UserInfo.defaultRoles.get(4))) {
			return true;
		}
		HttpServletRequest req = (HttpServletRequest) request;
		String method = req.getMethod();
		Hashtable<String, List<Pattern>> table = actions.get(method);
		if (table == null || table.isEmpty()) {
			return false;
		}
		try {
			URI uri = new URI(req.getRequestURI());
			String needle = uri.parseServerAuthority().toString();
			//check allowed anonymous access
			String role = UserInfo.defaultRoles.get(0);
			if (checkPattern(user, table.get(role), needle) == true) {
				return true;
			}
			Iterator<String> uroleit = user.roles().iterator();
			while (uroleit.hasNext()) {
				role = (String) uroleit.next();
				if (checkPattern(user, table.get(role), needle) == true) {
					return true;
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return false;
	}

}

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
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenID extends JWT implements UserInfo {
	String id_token;
	boolean validity;
	public int level;
	OpenIDServer OIDC;

	public OpenID(OpenIDServer OIDC, String data) throws JSONException, IOException {
		super(data, OIDC.getPublicKey());
		this.OIDC = OIDC;
		id_token = data;
		boolean result = false;
		Iterator<?> it = this.keys();
		while (it.hasNext()) {
			String elem = String.valueOf(it.next());
			if (elem.equals("exp")) {
				int expiration = super.getInt(elem);
				long currentTime = System.currentTimeMillis() / 1000L;
				result = (expiration > currentTime);
				if (!result)
					System.out.println("Data expired " + expiration + " / " + currentTime);
			}
			if (elem.equals("aud")) {
				String auditors = this.getString(elem);
				result = auditors.contains(OIDC.getClientId());
				if (!result)
					System.out.println("Bad client Id " + auditors + " / " + OIDC.getClientId());
			}
			validity = result;
		}
	}

	public OpenID() {
		super();
	}

	public OpenID(JSONObject object) {
		super(object);
	}

	public boolean isValid() {
		if (super.isValid())
			return validity;
		return false;
	}

	public void add(String name, String value) {
		try {
			if (has(name)) {
				remove(name);
			}
			append(name, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String token() {
		return id_token;
	}

	public boolean hasRole(String role) {
		boolean result = false;
		if (role.equalsIgnoreCase(defaultRoles.get(0)))
			return true;
		try {
			if (has("roles")) {
				JSONArray roles = getJSONArray("roles");
				int i;
				for (i = 0; i < roles.length(); i++) {
					if (roles.getString(i).equals(role)) {
						result = true;
						break;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean check(String token) {
		try {
			String previous = getString("access_token");
			if (previous == null) {
				add("access_token", token);
				return true;
			}
			return (previous.equals(token));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void dispose() {
		remove("access_token");
	}

	public boolean expire() {
		return false;
	}
}

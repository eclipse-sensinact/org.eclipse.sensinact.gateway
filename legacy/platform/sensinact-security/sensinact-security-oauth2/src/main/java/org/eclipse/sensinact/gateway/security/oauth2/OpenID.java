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
import java.math.BigDecimal;
import java.time.Instant;

import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class OpenID extends JWT implements UserInfo {
	String id_token;
	String access_token;
	public int level;
	OpenIDServer OIDC;

	public OpenID(OpenIDServer OIDC, String data) throws IOException {
		super(data, OIDC.getPublicKey());
		this.OIDC = OIDC;
		id_token = data;
	}

	public OpenID() {
		super();
	}

	public OpenID(JsonObject object) {
		super(object);
	}

	public boolean isValid() {
		if (super.isValid()) {
			JsonNumber exp = body.getJsonNumber("exp");
			if (exp != null) {
				Instant expiration = Instant.ofEpochSecond(exp.intValue());
				Instant now = Instant.now();
				if(expiration.isAfter(now)) {
					System.out.println("Data expired " + expiration + " / " + now);
					return false;
				}
			}
			String auditors = body.getString("aud", null);
			if (auditors != null) {
				if (!auditors.contains(OIDC.getClientId())) {
					System.out.println("Bad client Id " + auditors + " / " + OIDC.getClientId());
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public Object get(String field) {
		JsonValue jv = body.get(field);
		if(jv == null)
			return null;
		switch(jv.getValueType()) {
			case ARRAY:
				return jv.asJsonArray();
			case FALSE:
				return Boolean.FALSE;
			case NULL:
				return null;
			case NUMBER:
				BigDecimal bdv = ((JsonNumber) jv).bigDecimalValue();
				if(bdv.scale() <= 0) {
					return bdv.longValueExact();
				} else if(bdv.stripTrailingZeros().scale() <= 0) {
					return bdv.longValueExact();
				} else {
					return bdv.doubleValue();
				}
			case OBJECT:
				return jv.asJsonObject();
			case STRING:
				return ((JsonString)jv).getString();
			case TRUE:
				return Boolean.TRUE;
			default:
				break;
		}
		return null;
	}

	public synchronized void addAccessToken(String accessToken) {
		this.access_token = accessToken;
	}

	public String token() {
		return id_token;
	}

	public boolean hasRole(String role) {
		boolean result = false;
		if (role.equalsIgnoreCase(defaultRoles.get(0)))
			return true;
		try {
			result = roles().contains(role);
		} catch (JsonException e) {
			e.printStackTrace();
		}
		return result;
	}

	public synchronized boolean check(String token) {
		String previous = access_token;
		if (previous == null) {
			addAccessToken(token);
			return true;
		}
		return (previous.equals(token));
	}

	public synchronized void dispose() {
		access_token = null;
	}

	public boolean expire() {
		return false;
	}
}

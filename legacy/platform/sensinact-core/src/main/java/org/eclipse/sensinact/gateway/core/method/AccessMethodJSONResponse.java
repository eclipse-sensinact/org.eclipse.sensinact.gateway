/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.CastUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Extended {@link SnaMessage} dedicated to the responses to the
 * {@link AccessMethod}s invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AccessMethodJSONResponse extends AccessMethodResponse<JsonObject> {
	/**
	 * @param uri
	 * @param type
	 * @param status
	 */
	protected AccessMethodJSONResponse(String uri, Response type, Status status) {
		this(uri, type, status,
				(status == Status.SUCCESS) ? AccessMethodJSONResponse.SUCCESS_CODE : UNKNOWN_ERROR_CODE);
	}

	/**
	 * @param uri
	 * @param type
	 * @param status
	 */
	protected AccessMethodJSONResponse(String uri, Response type, Status status, int statusCode) {
		super(uri, type, status, statusCode);
	}

	/**
	 * @param key
	 * @return
	 */
	public JsonValue getResponse(String key) {
		return super.getResponse().get(key);
	}

	/**
	 * @param clazz
	 * @param key
	 * @return
	 */
	public <T> T getResponse(Class<T> clazz, String key) {
		JsonValue o = this.getResponse(key);
		return CastUtils.cast(clazz, o);
	}

}

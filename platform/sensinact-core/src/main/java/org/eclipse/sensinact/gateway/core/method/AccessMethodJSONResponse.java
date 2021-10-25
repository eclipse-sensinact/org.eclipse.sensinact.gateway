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
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;

/**
 * Extended {@link SnaMessage} dedicated to the responses to the
 * {@link AccessMethod}s invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AccessMethodJSONResponse extends AccessMethodResponse<JSONObject> {
	/**
	 * @param uri
	 * @param type
	 * @param status
	 */
	protected AccessMethodJSONResponse(Mediator mediator, String uri, Response type, Status status) {
		this(mediator, uri, type, status,
				(status == Status.SUCCESS) ? AccessMethodJSONResponse.SUCCESS_CODE : UNKNOWN_ERROR_CODE);
	}

	/**
	 * @param uri
	 * @param type
	 * @param status
	 */
	protected AccessMethodJSONResponse(Mediator mediator, String uri, Response type, Status status, int statusCode) {
		super(mediator, uri, type, status, statusCode);
	}

	/**
	 * @param key
	 * @return
	 */
	public Object getResponse(String key) {
		return super.getResponse().opt(key);
	}

	/**
	 * @param clazz
	 * @param key
	 * @return
	 */
	public <T> T getResponse(Class<T> clazz, String key) {
		Object o = this.getResponse(key);
		return CastUtils.cast(clazz, o);
	}

}

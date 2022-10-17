/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;

import jakarta.json.JsonArray;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 *
 * @param <S>
 */
public interface SnaErrorfulMessage<S extends Enum<S> & SnaMessageSubType & KeysCollection> extends SnaMessage<S> {
	static final int NO_ERROR = 200;
	static final int BAD_REQUEST_ERROR_CODE = 400;
	static final int FORBIDDEN_ERROR_CODE = 403;
	static final int NOT_FOUND_ERROR_CODE = 404;
	static final int TIMEOUT_ERROR_CODE = 408;
	static final int INTERNAL_SERVER_ERROR_CODE = 500;
	static final int UNKNOWN_ERROR_CODE = 520;

	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[] {
			new TypedKey<JsonArray>(SnaConstants.ERRORS_KEY, JsonArray.class, false) };

	/**
	 * @return
	 */
	JsonArray getErrors();

	/**
	 * @param errorsArray
	 */
	void setErrors(JsonArray errorsArray);

	/**
	 * @param exception
	 */
	void setErrors(Exception exception);
}

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

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.json.JSONObject;

/**
 * Extended {@link AccessMethodResponseBuilder} dedicated to {@link SetMethod}
 * execution
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public class SetResponseBuilder extends AccessMethodResponseBuilder<JSONObject, SetResponse> {
	/**
	 * @param uri
	 * @param parameters
	 */
	protected SetResponseBuilder(String uri, Object[] parameters) {
		super(uri, parameters);
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodResponseBuilder#
	 *      createAccessMethodResponse(org.eclipse.sensinact.gateway.core.model.message.SnaMessage.Status)
	 */
	@Override
	public SetResponse createAccessMethodResponse(Status status) {
		return new SetResponse(super.getPath(), status);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#getComponentType()
	 */
	@Override
	public Class<JSONObject> getComponentType() {
		return JSONObject.class;
	}
}

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONArray;

/**
 * Extended {@link AccessMethodJSONResponse} returned by an {@link ActMethod}
 * invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ActResponse extends AccessMethodJSONResponse {
	/**
	 * @param status
	 * @param code
	 */
	protected ActResponse(String uri, Status status) {
		this(uri, status,
				Status.SUCCESS.equals(status) ? SnaErrorfulMessage.NO_ERROR : SnaErrorfulMessage.UNKNOWN_ERROR_CODE);
	}

	/**
	 * @param status
	 */
	public ActResponse(String uri, Status status, int code) {
		super(uri, AccessMethodResponse.Response.ACT_RESPONSE, status, code);
	}

	/**
	 * Returns the JSONArray of JSON formated {@link Description} of the
	 * {@link StateVariableResource} modified during the execution of the ActMethod
	 * returning this {@link AccessMethodResponse}
	 * 
	 * @return the JSONArray of JSON formated {@link Description} of the modified
	 *         {@link StateVariableResource}
	 */
	@SuppressWarnings("unchecked")
	public JSONArray getTriggers() {
		List<String> triggered = this.<List<String>>get(SnaConstants.TRIGGERED_KEY);

		if (triggered == null) {
			triggered = new ArrayList<String>();
		}
		return new JSONArray(JSONUtils.toJSONFormat(triggered));
	}

	/**
	 * Add the JSON formated {@link Description} of a modified
	 * {@link StateVariableResource} to this {@link AccessMethodResponse}
	 * 
	 * @param trigger
	 *            the JSON formated {@link Description} of a modified
	 *            {@link StateVariableResource}
	 */
	public void addTriggered(String trigger) {
		List<String> triggered = this.<List<String>>get(SnaConstants.TRIGGERED_KEY);

		if (triggered == null) {
			triggered = new ArrayList<String>();
			super.putValue(SnaConstants.TRIGGERED_KEY, triggered);
		}
		triggered.add(trigger);
	}
}

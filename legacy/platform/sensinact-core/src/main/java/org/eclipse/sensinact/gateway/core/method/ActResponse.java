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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

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
	public JsonArray getTriggers() {
		List<JsonObject> triggered = this.<List<JsonObject>>get(SnaConstants.TRIGGERED_KEY);

		if (triggered == null) {
			triggered = new ArrayList<>();
		}
		
		JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder jab = provider.createArrayBuilder();
		for(JsonObject jo : triggered) {
			jab.add(jo);
		}
		return jab.build();
	}

	/**
	 * Add the JSON formated {@link Description} of a modified
	 * {@link StateVariableResource} to this {@link AccessMethodResponse}
	 * 
	 * @param trigger
	 *            the JSON formated {@link Description} of a modified
	 *            {@link StateVariableResource}
	 */
	public void addTriggered(JsonObject trigger) {
		List<JsonObject> triggered = this.<List<JsonObject>>get(SnaConstants.TRIGGERED_KEY);

		if (triggered == null) {
			triggered = new ArrayList<>();
			super.putValue(SnaConstants.TRIGGERED_KEY, triggered);
		}
		triggered.add(trigger);
	}
}

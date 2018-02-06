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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import java.io.IOException;

import org.json.JSONObject;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.JSONResponseFormat;

/**
 * Extended {@link NorthbundAccess} dedicated to websocket connections
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class WsRestAccess extends NorthboundAccess
{
	/**
	 * The {@link WebSocketWrapper} held by this WsRestAccess 
	 */
	private WebSocketWrapper socket;

	/**
	 * Constructor
	 * 
	 * @param socket the {@link WebSocketWrapper} held by 
	 * the WsRestAccess to be instantiated
	 */
	public WsRestAccess(WebSocketWrapper socket)
	{
		this.socket = socket;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess#respond(org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder)
	 */
	@Override
	protected boolean respond(NorthboundRequestBuilder<JSONObject> builder) 
			throws IOException 
	{
		NorthboundRequest<JSONObject> nthbndRequest = builder.build();
		if(nthbndRequest == null)
		{
			sendError(500, "Internal server error");
			return false;
		}
		JSONObject result = this.endpoint.execute(nthbndRequest, 
				new JSONResponseFormat(mediator));
		
		if(result == null)
		{
			sendError(500, "Internal server error");
			return false;
		}
		result.put("X-Auth-Token", super.endpoint.getSessionToken());
		result.put("rid", super.rid);
		
		this.socket.send(new String(result.toString().getBytes("UTF-8")));
		return true;

	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess#sendError(int, java.lang.String)
	 */
	@Override
	protected void sendError(int i, String string) throws IOException 
	{
		JSONObject jsonObject = new JSONObject();
		if(super.endpoint!=null)
		{
			jsonObject.put("X-Auth-Token", super.endpoint.getSessionToken());
		}
		jsonObject.put("statusCode", i);
		jsonObject.put("message", string);
		this.socket.send(new String(jsonObject.toString().getBytes("UTF-8")));
	}

}

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
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;

/**
 * Session wrapper
 * 
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketWrapper
{

	protected Session session;
	protected NorthboundMediator mediator;
	protected WebSocketWrapperPool pool;
 
	
	protected WebSocketWrapper(WebSocketWrapperPool pool, NorthboundMediator mediator)
	{
		this.mediator = mediator;
		this.pool = pool;
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session)
	{ 			
		this.session = session;	
	}
	
	/** 
	 * @inheritedDoc
	 * 
	 * @see WebSocketWrapper#
	 * onClose(int, java.lang.String)
	 */
	@OnWebSocketClose
	public void onClose(int statusCode, String reason)
	{
		this.pool.removeSensinactSocket(this);
	}

	/**
	 * Closes this WebSocketWrapper and all its resources
	 */
	protected void close()
	{
		if(this.session == null)
		{
			return;
		}
		if(this.session.isOpen())
		{
			this.session.close();
		}
		this.session = null;
	}
	
	/** 
	 * Receives Text Message events
	 *
	 * @param message the received message
	 */
	@OnWebSocketMessage
	public void onMessage(String message)
	{
		try
		{
			JSONObject jsonObject = new JSONObject(message);
			WsRestAccessRequest wrapper = new WsRestAccessRequest(
					mediator, this, jsonObject);
			
			WsRestAccess restAccess = new WsRestAccess(this);
			restAccess.init(wrapper);
			restAccess.handle();
			
		} catch(JSONException e)
		{
			this.mediator.error(e.getMessage(),e);
			this.send(new JSONObject().put("statusCode", 400
					).put("message","Bad request"
							).toString());
		} catch (IOException e) 
		{
			this.mediator.error(e.getMessage(),e);
			this.send(new JSONObject().put("statusCode", 400
					).put("message","Bad request"
							).toString());
		} catch (Exception e) 
		{
			this.mediator.error(e);
			this.send(new JSONObject().put("statusCode", 500
					).put("message","Internal server error"
							).toString());
		}
	}
	
	/**
	 *  Receives websocket errors (exceptions) that have 
	 *  occurred internally in the websocket implementation.
	 *  
	 * @param error the exception that occurred
	 */
	@OnWebSocketError
	public void handleError(Throwable error)
	{
		error.printStackTrace();
	}
	
	/**
	 * @param message
	 */
	protected void send(String message)
	{
		if(this.session == null)
		{
			return;
		}
		try 
		{
           Future<Void> future = this.session.getRemote(
        	).sendStringByFuture(
        		   message);
           
           future.get(1, TimeUnit.SECONDS);
            
        } catch (Exception e) 
        {
            this.mediator.error(new StringBuilder().append(
            	"Session ").append(session.getLocalAddress()).append(
            	"seems to be invalid, removing from the pool."
            		).toString(),e);      	
        }
	}

	/**
	 * @return
	 */
	public InetSocketAddress getClientAddress()
	{
		return this.session.getRemoteAddress();
	}
}

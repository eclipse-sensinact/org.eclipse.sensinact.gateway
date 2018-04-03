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
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * WebSocket connection endpoint
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketConnection
{
	protected static final String LOGIN_PATH = "login";
	protected static final String LOGIN_URI = "/" + LOGIN_PATH;
	
	protected Session session;
	protected NorthboundMediator mediator;
	protected WebSocketConnectionFactory pool;
	protected NorthboundEndpoint endpoint;
 
	/**
	 * @param pool
	 * @param endpoint
	 * @param mediator
	 */
	protected WebSocketConnection(
			WebSocketConnectionFactory pool, 
			NorthboundEndpoint endpoint,
			NorthboundMediator mediator)
	{
		this.endpoint = endpoint;
		this.mediator = mediator;
		this.pool = pool;
	}
	
	/**
	 * @param session
	 */
	@OnWebSocketConnect
	public void onConnect(Session session)
	{ 			
		this.session = session;	
	}
	
	
	/** 
	 * @inheritedDoc
	 * 
	 * @see WebSocketConnection#
	 * onClose(int, java.lang.String)
	 */
	@OnWebSocketClose
	public void onClose(int statusCode, String reason)
	{
		this.pool.deleteSocketEndpoint(this);
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
	 * @return
	 */
	NorthboundEndpoint getEndpoint()
	{
		return this.endpoint;
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
			
			WsRestAccess restAccess = new WsRestAccess(wrapper, this);
			restAccess.proceed();
			
		} catch(IOException | JSONException e)
		{
			this.mediator.error(e);
			this.send(new JSONObject().put("statusCode", 400
				).put("message","Bad request"
					).toString());
		}catch (InvalidCredentialException e) 
		{
			this.mediator.error(e);
			this.send(new JSONObject().put("statusCode", 403
				).put("message",e.getMessage()
					).toString());
		} catch (Exception e) 
		{
			this.mediator.error(e);
			this.send(new JSONObject().put("statusCode", 500
			).put("message","Exception - Internal server error"
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
	 * @param message
	 */
	protected void send(byte[] message)
	{
		if(this.session == null)
		{
			return;
		}
	
		try 
		{
           Future<Void> future = this.session.getRemote(
        	).sendBytesByFuture(ByteBuffer.wrap(message));
           
           future.get(1, TimeUnit.SECONDS);
            
        } catch (Exception e) 
        {
            this.mediator.error(new StringBuilder().append(
            	"Session ").append(session.getLocalAddress()).append(
            	"seems to be invalid, removing from the pool."
            		).toString(),e);      	
        }
	}
}

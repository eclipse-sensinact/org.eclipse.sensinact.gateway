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
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletOutputStream;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.LoginResponse;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;

/**
 * Session wrapper
 * 
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketWrapper
{
	protected static final String LOGIN_PATH = "login";
	protected static final String LOGIN_URI = "/" + LOGIN_PATH;
	
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
			
			String uri = jsonObject.getString("uri");
			
			if(uri.equals(LOGIN_PATH)||uri.equals(LOGIN_URI))
			{
				onLogin(wrapper);
				return;
			}
			WsRestAccess restAccess = new WsRestAccess(wrapper, this);
			restAccess.proceed();
			
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
			e.printStackTrace();
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

	private void onLogin(WsRestAccessRequest wrapper)
	{
		Authentication<?> authentication = wrapper.getAuthentication();
		LoginResponse loginResponse = null;	

		if(AuthenticationToken.class.isAssignableFrom(
				authentication.getClass()))
		{			
			loginResponse = mediator.getLoginEndpoint(
			).reactivateEndpoint((AuthenticationToken) authentication);
										
		} else if(Credentials.class.isAssignableFrom(
				authentication.getClass()))
		{
			loginResponse = mediator.getLoginEndpoint(
			).createNorthboundEndpoint((Credentials)authentication);
		}
		this.send(loginResponse.getJSON());		
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
	
	/**
	 * @return
	 */
	public InetSocketAddress getClientAddress()
	{
		return this.session.getRemoteAddress();
	}
}

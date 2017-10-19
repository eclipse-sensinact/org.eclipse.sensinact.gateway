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
package org.eclipse.sensinact.gateway.nthbnd.rest.ws.test;

import java.net.URI;
import java.util.Stack;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws.WebSocketWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class WsServiceTestClient implements Runnable
{

	String destUri = "ws://localhost:8093/ws/sensinact";
	WebSocketClient client = null;
	Session session;
	AtomicBoolean available;
	private String lastMessage;

	
	public WsServiceTestClient()
	{
		this.available = new AtomicBoolean(false);
        try 
        {
        	client = new WebSocketClient();
        	client.setMaxIdleTimeout(1000*3600);
            
            client.start();
            URI echoUri = new URI(destUri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            
            Future<Session> future = client.connect(this, echoUri, request);
            /*Session session = */future.get(2, TimeUnit.SECONDS);
            //this.socket.onConnect(session);
            
        } catch (Throwable t)
        {
            t.printStackTrace();
            try 
            {
                client.stop();
            } catch (Exception e) { }
        } 
	}


	public void setAvailable(boolean available) 
	{
		this.available.set(available);
	}
	
	public boolean isAvailable() 
	{
		return this.available.get();
	}

	public String getResponseMessage() 
	{
		return this.lastMessage;
	}
	
	public void newRequest(String url, String content) 
	{
		this.setAvailable(false);
		this.lastMessage = null;
		
		synchronized(this.stack)
		{
			this.stack.push(new WsRequest(url,content));
		}
	}	

	/**
	 * @param message
	 */
	protected void send(String message)
	{
		try 
        {
           Future<Void> future = this.session.getRemote(
        	).sendStringByFuture(
        		   message);
           
           future.get(1, TimeUnit.SECONDS);
            
        } catch (Exception e) 
        {
        	e.printStackTrace();      	
        }
	}
	

	@OnWebSocketConnect
	public void onConnect(Session session) 
	{
		this.session = session;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see WebSocketWrapper#onClose(int, java.lang.String)
	 */
	@OnWebSocketClose
	public void onClose(int statusCode, String reason)
	{
		this.session = null;
		this.close();
	}

	public void close()
	{
		this.running = false;
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see WebSocketWrapper#onMessage(java.lang.String)
	 */
	@OnWebSocketMessage
	public void onMessage(String message) 
	{
		this.setAvailable(true);
		this.lastMessage = message;
	}

	@OnWebSocketError
	public void handleError(Throwable error)
	{
		error.printStackTrace();
	}


	private boolean running = false;
	private Stack<WsRequest> stack = new Stack<WsRequest>();
	
	@Override
	public void run()
	{
		running = true;
		boolean locked = true;
		WsRequest request = null;
		
		while(true)
		{			
			if(!running && !locked)
			{
				break;
			}	
			request = null;
			
			synchronized(this.stack)
			{
				if(!this.stack.isEmpty())
				{
					request = this.stack.pop();
				}
				locked = !this.stack.isEmpty();
			}
			if(request != null)
			{
				JSONObject json =  new JSONObject();
		        json.put("uri", request.url);
		        if(request.content != null)
		        {
		        	json.put("parameters",
		        		new JSONArray(request.content));
		        }
		        this.send(json.toString());				
			} else
			{
				try 
				{
					Thread.sleep(200);
					
				} catch (InterruptedException e)
				{
					Thread.interrupted();
					break;
				}
			}
		}
		try 
		{
			this.client.stop();
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
    class WsRequest
    {
    	public final String url;
    	public final String content;
    	
    	WsRequest(String url, String content)
    	{
    		this.url = url;
    		this.content = content;
    	}
    }
}

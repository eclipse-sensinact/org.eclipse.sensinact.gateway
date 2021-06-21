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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.common.OpCode;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * WebSocket connection endpoint
 */
@WebSocket(maxIdleTime = 0, maxTextMessageSize = 64 * 1024)
public class WebSocketConnection {
    protected static final String LOGIN_PATH = "sensinact-login";
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
    protected WebSocketConnection(WebSocketConnectionFactory pool, NorthboundEndpoint endpoint, NorthboundMediator mediator) {
        this.endpoint = endpoint;
        this.mediator = mediator;
        this.pool = pool;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.pool.deleteSocketEndpoint(this);
    }
    
    protected void close() {
        if (this.session == null) {
            return;
        }
        if (this.session.isOpen()) {
        	try {
        		this.session.close();
        	} catch(Exception e) {
        		this.mediator.error(e);
        	}
        }
        this.session = null;
    }

    NorthboundEndpoint getEndpoint() {
        return this.endpoint;
    }

    /**
     * Receives Text Message events
     *
     * @param message the received message
     */
    @OnWebSocketMessage
    public void onMessage(String message) {
    	if(partial)
    		return;
        try {
            JSONObject jsonObject = new JSONObject(message);
            WsRestAccessRequest wrapper = new WsRestAccessRequest(mediator, this, jsonObject);
            WsRestAccess restAccess = new WsRestAccess(wrapper, this);
            restAccess.proceed();
        } catch (IOException | JSONException e) {
            this.mediator.error(e);
            try {
				this.send(new JSONObject().put("statusCode", 400).put("message", "Bad request").toString());
			} catch (Exception e1) {
	            this.mediator.error(e1);
			}
        } catch (InvalidCredentialException e) {
            this.mediator.error(e);
            try {
				this.send(new JSONObject().put("statusCode", 403).put("message", e.getMessage()).toString());
			} catch (Exception e1) {
	            this.mediator.error(e1);
			}
        } catch (Exception e) {
        	e.printStackTrace();
            this.mediator.error(e);
            try {
				this.send(new JSONObject().put("statusCode", 500).put("message", "Exception - Internal server error").toString());
			} catch (Exception e1) {
	            this.mediator.error(e1);
			}
        }
    }

    private boolean partial;
	private byte[] payload; 
	
	@OnWebSocketFrame
	public void onFrame(Frame frame) {		
		switch(frame.getOpCode()) {		
			case OpCode.PING :
				partial = true;
				try {
					this.session.getRemote().sendPong(ByteBuffer.allocate(0));
				} catch (IOException e) {
		            this.mediator.error(e);
				}
				break;
			case OpCode.PONG :
				partial = true;
				break;
			case OpCode.CONTINUATION:
				partial = true;
				byte[] bytes = new byte[frame.getPayloadLength()];
				frame.getPayload().get(bytes);
				int length = payload==null?0:payload.length;
				byte[] tmpArray = new byte[length+bytes.length] ;
				if(bytes.length > 0)
					System.arraycopy(bytes,0, tmpArray, length, bytes.length);
				if(length > 0)
					System.arraycopy(payload, 0, tmpArray, 0, length);
				payload = tmpArray;	
				tmpArray = null;
				if(frame.isFin()) {
					partial = false;
					onMessage(new String(payload));
					partial = true;
					payload = null;
				}
				break;
			default:
				if(frame.isFin())
					partial = false;
				break;
		}
	}
	
    @OnWebSocketError
    public void handleError(Throwable error) {
        error.printStackTrace();
    }
    
    protected void send(String message) throws Exception {
        if (this.session == null)
            return;
        try {
        	if(message.length() > 65536) {
        		int index = 0;
        		int offset = 32000;
        		String piece = null;
        		while(index < message.length()) {
        			if(index+offset > message.length())
        				offset = message.length()-index;
        			piece = message.substring(index,index+offset);
        			this.session.getRemote().sendPartialString(piece,(index+offset)==message.length()?true:false);
        			index+=offset;
        		}        		
        	} else {
        		Future<Void> future = this.session.getRemote().sendStringByFuture(message);
        		future.get(1, TimeUnit.SECONDS);
        	}
        } catch (Exception e) {
            this.mediator.error(new StringBuilder(
            	).append("Session "
            	).append(session.getLocalAddress()
            	).append("seems to be invalid, removing from the pool."
            	).toString(), e);
            pool.deleteSocketEndpoint(this);
            throw e;
        }
    }

    protected void send(byte[] message) throws Exception {
        if (this.session == null)
            return;
        try {
        	if(message.length > 65536) {
	    		int index = 0;
	    		int offset = 32000;
	    		while(index < message.length) {
	    			if(index+offset > message.length)
	    				offset = message.length-index;
		    		ByteBuffer buffer = ByteBuffer.wrap(message, index, offset);
	    			this.session.getRemote().sendPartialBytes(buffer,(index+offset==message.length)?true:false);
	    			index+=offset;
	    		}        		
	    	} else {
	    		Future<Void> future = this.session.getRemote().sendBytesByFuture(ByteBuffer.wrap(message));
	    		future.get(1, TimeUnit.SECONDS);
	    	}
        } catch (Exception e) {
            this.mediator.error(new StringBuilder(
            		).append("Session "
            		).append(session.getLocalAddress()
            		).append("seems to be invalid, removing from the pool."
            		).toString(), e);
            pool.deleteSocketEndpoint(this);
            throw e;
        }
    }
}

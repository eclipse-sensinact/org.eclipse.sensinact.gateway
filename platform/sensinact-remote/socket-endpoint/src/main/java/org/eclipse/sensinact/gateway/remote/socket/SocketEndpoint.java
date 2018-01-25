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

package org.eclipse.sensinact.gateway.remote.socket;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Simple RemoteEndpoint implementation using socket connection 
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SocketEndpoint extends AbstractRemoteEndpoint
{
	public final static int BUFFER_SIZE = 64*1024;	
	public final static int PORT = 54460;
	public final static int MAGIC = 0xDEADBEEF;


	static byte[] intToBytes(int l) 
	{
	    byte[] result = new byte[8];
	    for (int i = 7; i >= 0; i--) {
	        result[i] = (byte)(l & 0xFF);
	        l >>= 8;
	    }
	    return result;
	}

	static int bytesToInt(byte[] b) 
	{
	    int result = 0;
	    for (int i = 0; i < 8; i++) {
	        result <<= 8;
	        result |= (b[i] & 0xFF);
	    }
	    return result;
	}

	protected String remoteNamespace;
	protected ClientSocketThread client;
	protected ServerSocketThread server;

	private final String localAddress;
	private final int localPort;
	
	private final String remoteAddress;
	private final int remotePort;
	
	private Timer connectionTimer;
	//TimerTask connectionTask  = 
			
	
	

	/**
	 * @param mediator
	 * @param localNamespace
	 * @param localAddress
	 * @param localPort
	 * @param remoteAddress
	 * @param remotePort
	 */
	public SocketEndpoint(Mediator mediator, String localNamespace, 
			String localAddress, int localPort, 
			String remoteAddress, int remotePort) 
	{
		super(mediator, localNamespace);
		if(localAddress == null || remoteAddress == null)
		{
			throw new NullPointerException("Local and remote addresses are needed");
		}
		this.localAddress = localAddress;
		this.localPort = localPort <= 0?80:localPort;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort <= 0?80:remotePort;
	}

	/**
	 * @return 
	 *     the localAddress
	 */
	protected String getLocalAddress()
	{
		return localAddress;
	}
	
	/**
	 * @return 
	 *     the localPort
	 */
	protected int getLocalPort()
	{
		return localPort;
	}

	/**
	 * @return 
	 *     the remoteAddress
	 */
	protected String getRemoteAddress()
	{
		return remoteAddress;
	}

	/**
	 * @return 
	 *     the remotePort
	 */
	protected int getRemotePort()
	{
		return remotePort;
	}

	/**
	 * @param request
	 * @return
	 */
	protected JSONObject incomingRequest(JSONObject request)
	{
		JSONObject response  = null;
		if(request == null)
		{
			return null;
		}
		String uri = request.optString("uri");
		String uuid = request.optString("uuid");
		String publicKey = request.optString("pkey");
		
		String[] uriElements = UriUtils.getUriElements(uri);
		int length = uriElements.length;
	
		switch(length)
		{
			case 1:			
				String[] subUriELements = uriElements[0].split("\\?");
				switch(subUriELements[0])
				{
					case "namespace":
						response  = new JSONObject().put("response", 
					        new JSONObject().put("value", super.localNamespace));
					break;
					case "agent":
						if(subUriELements.length != 2)
						{
							break;
						}
						String agentId = subUriELements[1];
						JSONObject object = request.optJSONObject("agent");
						if(JSONObject.NULL.equals(object))
						{								
							object = request.optJSONObject("message");
							if(JSONObject.NULL.equals(object))
							{
								super.remoteCore.unregisterAgent(agentId);
								
							} else
							{
								super.remoteCore.dispatch(agentId, 
								    AbstractSnaMessage.fromJSON(mediator, 
										object.toString()));
							}
						} else
						{
							JSONObject f = object.optJSONObject("filter");
							
							SnaFilter filter = new SnaFilter(mediator,
							    f.getString("sender"),
								f.optBoolean("pattern"), 
								f.optBoolean("complement"),
								f.optJSONArray("conditions"));
							
							JSONArray t = object.optJSONArray("types");
							int i = 0;								
							int l = t==null?0:t.length();
							for(; i < l; i++)
							{
								filter.addHandledType(
									SnaMessage.Type.valueOf(t.getString(i)));
							}
							super.remoteCore.registerAgent(agentId, 
							   filter, object.getString("agentKey"));
						}
					break;
					case "session":
						super.remoteCore.closeSession(publicKey);
					break;
					case "callback":
						if(subUriELements.length != 2)
						{
							break;
						}
						String callbackId = subUriELements[1];
						Recipient recipient = super.recipients.get(callbackId);
						if(recipient == null)
						{
							break;
						}
						JSONArray messagesArray = request.optJSONArray("messages");
						int index = 0;
						int arrayLength = messagesArray==null?0:messagesArray.length();
						SnaMessage<?>[] messages = new SnaMessage[arrayLength];
						try 
						{
							for(;index < arrayLength;index++)
							{
								messages[index]= AbstractSnaMessage.fromJSON(mediator,
									messagesArray.getJSONObject(index).toString());
							}
							recipient.callback(callbackId, messages);
							
						} catch (Exception e) 
						{
							mediator.error(e.getMessage(),e);
						}			
					break;
					case "locations":
						response  = super.remoteCore.getLocations(publicKey);
					break;
					case "model":
						if(subUriELements.length > 2)
						{
							break;
						}
						response  = (subUriELements.length == 1)?
							super.remoteCore.getAll(publicKey)
					       :super.remoteCore.getAll(publicKey,subUriELements[1]);						
					break;
					case "providers":
						response = super.remoteCore.getProviders(publicKey);
					break;
				}
				break;
			case 2:
				response = super.remoteCore.getProvider(publicKey,
					uriElements[1]);
				break;
			case 3:
				response = super.remoteCore.getServices(publicKey,
					uriElements[1]);
				break;
			case 4:
				response = super.remoteCore.getService(publicKey, 
					uriElements[1],uriElements[3]);
				break;
			case 5:
				response = super.remoteCore.getResources(publicKey,
					uriElements[1],uriElements[3]);
				break;
			case 6:
				response = super.remoteCore.getResource(publicKey, 
					uriElements[1],uriElements[3],uriElements[5]);
				break;
			case 7:
				switch(uriElements[6])
				{
					case "GET":				
					    response = super.remoteCore.get(publicKey, 
							uriElements[1],uriElements[3],uriElements[5], 
							null);
					break;				
					case "SET":
						JSONArray parameters = request.optJSONArray("parameters");
						JSONObject attr = parameters.optJSONObject(0);
						String attributeId = null;
						if(attr != null)
						{
						 attributeId = attr.optString("value");
						}
						attr = parameters.optJSONObject(1);
						Object data = null;
						if(attr != null)
						{
						 data = attr.opt("value");
						}
						response = super.remoteCore.set(publicKey,
							uriElements[1],uriElements[3],uriElements[5],
								attributeId,data);
					break;				
					case "ACT":				
						parameters = request.optJSONArray("parameters");
						int index = 0;
						int paramsLength = parameters==null?0:parameters.length();
						
						Object[] args = new Object[paramsLength];
						for(;index < paramsLength;index++)
						{
							args[index]=parameters.getJSONObject(index).opt("value");
						}
						response = super.remoteCore.act(publicKey, 
							uriElements[1],uriElements[3],uriElements[5], 
								args);
					break;
					case "SUBSCRIBE":				
						JSONArray conditions = null;
						parameters = request.optJSONArray("parameters");
						paramsLength = parameters==null?0:parameters.length();
						if(paramsLength == 1)
						{
							conditions = parameters.getJSONObject(0
									).optJSONArray("value");
						}
						response = super.remoteCore.subscribe(publicKey,
							uriElements[1],uriElements[3],uriElements[5], 
							    conditions);
					break;
					case "UNSUBSCRIBE":				
						String subscriptionId = null;
						parameters = request.optJSONArray("parameters");
						paramsLength = parameters==null?0:parameters.length();
						if(paramsLength == 1)
						{
							subscriptionId = parameters.getJSONObject(0
									).optString("value");
						}
						response = super.remoteCore.unsubscribe(publicKey, 
							uriElements[1],uriElements[3],uriElements[5], 
							   subscriptionId);
					break;
					default: break;
				}
			default:
				break;
		}
		if(response == null)
		{
			response = new JSONObject();
		}
		response.put("uuid", uuid);
		return response;
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#namespace()
	 */
	@Override
	public String namespace()
	{
		if(this.remoteNamespace == null && this.client != null)
		{
			JSONObject content = null;
			JSONObject response = this.client.request(
					new JSONObject().put("uri", "/namespace"));
			if(response != null && 
				(content = response.optJSONObject("response"))!=null)
			{
				this.remoteNamespace = (String) content.opt("value");
			}
		}
		return this.remoteNamespace;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.Recipient#
	 * callback(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaMessage[])
	 */
	@Override
	public void callback(String callbackId, SnaMessage[] messages) 
	{
		if(!super.connected)
		{
			return;
		}
		String uri = String.format("/callback?%s",callbackId);
		JSONArray messagesArray = new JSONArray();
		int index = 0;
		int length = messages==null?0:messages.length;
		for(;index < length;index++)
		{
			messagesArray.put(new JSONObject(messages[index].getJSON()));
		}
		JSONObject response = this.client.request(
			new JSONObject(
				).put("uri", uri
				).put("messages", messagesArray));

		if(!JSONObject.NULL.equals(response))
		{
			mediator.debug(response.toString());
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
	 */
	@Override
	public String getJSON()
	{
		return String.format("{\"local\":\"%s\",\"remote\":\"%s\"}",
				super.getLocalNamespace(),
				this.namespace());
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#doConnect()
	 */
	@Override
	public void doConnect() 
	{
		if(this.remoteNamespace!=null)
		{
			return;
		}
		if(this.server==null)
		{
			try
			{
				this.server = new ServerSocketThread(mediator, this);				
				new Thread(server).start();
			}
			catch (IOException e)
			{
				mediator.error(e);
				super.connected = false;
				return;
			}
		}
		this.connectionTimer = new Timer();
		this.connectionTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{			
				try 
				{
					if(SocketEndpoint.this.client == null)
					{
						SocketEndpoint.this.client = new ClientSocketThread(mediator,
								SocketEndpoint.this.getRemoteAddress(), 
								SocketEndpoint.this.getRemotePort());
					}
					if(!SocketEndpoint.this.client.running())
					{
						new Thread(SocketEndpoint.this.client).start();					
						SocketEndpoint.this.mediator.debug("Client socket thread started");	
					}
					if(SocketEndpoint.this.namespace()!=null)
					{ 
						mediator.debug(
						"Client socket thread connected - \n\tremote namespace : %s", 
						SocketEndpoint.this.remoteNamespace);					
						SocketEndpoint.this.connectionTimer.cancel();
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					if(SocketEndpoint.this.client!=null 
							&& !SocketEndpoint.this.client.running())
					{
						SocketEndpoint.this.client = null;
					}
				}
			}
		}, 0, 1000*10);		
		int timeout = 60*3000;
		
		while(timeout > 0 && remoteNamespace == null)
		{
			this.namespace();
			try
			{
				timeout-=100;
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				Thread.interrupted();
				break;
			}
		}		
		this.connectionTimer.cancel();
		this.connectionTimer = null;
		if(remoteNamespace == null)
		{
			super.connected = false;
		}
		mediator.info("%s is%sconnected%s%s", 
			super.localNamespace, (super.connected?" ":" not "),
			(super.connected?" to ":""),(super.connected?remoteNamespace:""));
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#
	 * doDisconnect()
	 */
	@Override
	protected void doDisconnect() 
	{
		this.server.stop();
		this.server = null;
		
		this.client.stop();
		this.client = null;
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
	 * registerAgent(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaFilter, java.lang.String)
	 */
	@Override
	public void registerAgent(String identifier, SnaFilter filter, String agentKey)
	{
		if(!super.connected)
		{
			return;
		}
		String uri = String.format("/agent?%s", identifier);
		
		JSONObject response = this.client.request(
			new JSONObject(
				).put("uri", uri
				).put("agent", new JSONObject(
					).put("agentKey", agentKey
				    ).put("filter", filter.toJSONObject())));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
	 * unregisterAgent(java.lang.String)
	 */
	@Override
	public void unregisterAgent(String identifier)
	{
		if(!super.connected)
		{
			return;
		}
		String uri = String.format("/agent?%s",identifier);
		
		JSONObject response = this.client.request(
			new JSONObject().put("uri", uri));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
	 * dispatch(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaMessage)
	 */
	@Override
	public void dispatch(String agentId, SnaMessage<?> message)
	{
		if(!super.connected)
		{
			return;
		}
		String uri = String.format("/agent?%s",agentId);
		
		JSONObject response = this.client.request(
			new JSONObject(
				).put("uri", uri
				).put("message", new JSONObject(message.getJSON())));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#
	 * closeSession(java.lang.String)
	 */
	@Override
	protected void closeSession(String publicKey)
	{
		if(!super.connected)
		{
			return;
		}
		JSONObject response = this.client.request(
			new JSONObject(
			    ).put("uri", "/session"
			    ).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getAll(java.lang.String)
	 */
	@Override
	public JSONObject getAll(String publicKey)
	{
		if(!super.connected)
		{
			return null;
		}
		return this.getAll(publicKey, null);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getAll(java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getAll(String publicKey,String filter) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri = String.format("/model?%s",filter);

		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getLocations(java.lang.String)
	 */
	@Override
	public JSONObject getLocations(String publicKey)
	{		
		if(!super.connected)
		{
			return null;
		}
		String uri ="/locations";

		JSONObject response = this.client.request(
			new JSONObject(
				).put("uri", uri
				).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getProviders(java.lang.String)
	 */
	@Override
	public JSONObject getProviders(String publicKey)
	{
		if(!super.connected)
		{
			return null;
		}
		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", "/providers"
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getProvider(java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getProvider(String publicKey, 
			String serviceProviderId)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri = String.format("/providers/%s",serviceProviderId);

		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getServices(java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getServices(String publicKey, 
			String serviceProviderId)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services", serviceProviderId);

		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getService(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getService(String publicKey, 
			String serviceProviderId, String serviceId)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s", 
				serviceProviderId,serviceId);

		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getResources(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getResources(String publicKey, 
			String serviceProviderId, String serviceId) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources", 
				serviceProviderId,serviceId);

		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getResource(String publicKey, 
			String serviceProviderId, String serviceId, 
			String resourceId) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources/%s", 
				serviceProviderId,serviceId,resourceId);

		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject get(String publicKey, 
			String serviceProviderId, String serviceId, 
			String resourceId, String attributeId)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources/%s/GET", 
				serviceProviderId,serviceId,resourceId);

		JSONObject response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * set(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public JSONObject set(String publicKey, 
			String serviceProviderId, String serviceId, 
			String resourceId, String attributeId, Object parameter) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources/%s/SET", 
				serviceProviderId,serviceId,resourceId);

		JSONArray parameters = new JSONArray();
		JSONObject object = new JSONObject();		
		object.put("name", "attributeName");
		object.put("type", "string");
		object.put("value", attributeId==null?"value":attributeId);
		parameters.put(object);
		
		Object value = JSONUtils.toJSON(parameter);
		object = new JSONObject();		
		object.put("name", "arg0");
		object.put("type", value.getClass().getName());
		object.put("value", value);
		parameters.put(object);
		
		JSONObject response = this.client.request(
			new JSONObject(
			).put("uri", uri
			).put("pkey", publicKey
			).put("parameters", parameters));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * act(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	@Override
	public JSONObject act(String publicKey, 
			String serviceProviderId, String serviceId, 
			String resourceId, Object[] parameters) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources/%s/ACT", 
				serviceProviderId,serviceId,resourceId);

		JSONArray parametersArray = new JSONArray();
		int index = 0;
		int length = parameters==null?0:parameters.length;
		for(;index < length;index++)
		{
			Object value = JSONUtils.toJSON(parameters[index]);
			JSONObject object = new JSONObject();		
			object.put("name", "arg"+index);
			object.put("type", value.getClass().getName());
			object.put("value", value);
			parametersArray.put(object);
		}		
		JSONObject response = this.client.request(
				new JSONObject(
				).put("uri", uri
			    ).put("pkey", publicKey
				).put("parameters", parametersArray));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * unsubscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject unsubscribe(String publicKey, 
			String serviceProviderId, String serviceId, 
			String resourceId, String subscriptionId) 
	{   
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources/%s/UNSUBSCRIBE", 
				serviceProviderId,serviceId,resourceId);

		JSONArray parametersArray = new JSONArray();
		JSONObject object = new JSONObject();		
		object.put("name", "subscriptionId");
		object.put("type", "string");
		object.put("value", subscriptionId);
		parametersArray.put(object);

		JSONObject response = this.client.request(
				new JSONObject(
			    ).put("uri", uri
			    ).put("pkey", publicKey
				).put("parameters", parametersArray));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#
	 * doSubscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.json.JSONArray)
	 */
	@Override
	protected JSONObject doSubscribe(String publicKey, 
			String serviceProviderId, String serviceId, 
			String resourceId, JSONArray conditions)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources/%s/SUBSCRIBE", 
				serviceProviderId,serviceId,resourceId);

		JSONArray parametersArray = new JSONArray();
		JSONObject object = new JSONObject();		
		object.put("name", "conditions");
		object.put("type", "array");
		object.put("value", conditions);
		parametersArray.put(object);

		JSONObject response = this.client.request(
				new JSONObject(
			    ).put("uri", uri
				).put("pkey", publicKey
				).put("parameters", parametersArray));

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
		return response;
	}
}
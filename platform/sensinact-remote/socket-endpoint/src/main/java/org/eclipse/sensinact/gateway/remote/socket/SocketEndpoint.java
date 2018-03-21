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
import org.eclipse.sensinact.gateway.core.RemoteCore;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
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

	/**
	 * @param mediator
	 * @param localNamespace
	 * @param localAddress
	 * @param localPort
	 * @param remoteAddress
	 * @param remotePort
	 */
	public SocketEndpoint(Mediator mediator, 
			String localAddress, int localPort, 
			String remoteAddress, int remotePort) 
	{
		super(mediator);
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
		String response  = null;
		JSONObject accessMethodResponse = null;
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
						response  = super.localNamespace; 
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
							SnaFilter filter = null;
							JSONObject f = object.optJSONObject("filter");
							if(!JSONObject.NULL.equals(f))
							{
								filter = new SnaFilter(mediator,
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
								messages[index]= AbstractSnaMessage.fromJSON(
									mediator, messagesArray.getJSONObject(
										index).toString());
							}
							recipient.callback(callbackId, messages);
							
						} catch (Exception e) 
						{
							mediator.error(e);
						}			
					break;
					case "all":
						if(subUriELements.length > 2)
						{
							break;
						}
						response = (subUriELements.length == 1)?
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
						accessMethodResponse = super.remoteCore.get(publicKey, 
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
						accessMethodResponse = super.remoteCore.set(publicKey,
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
						accessMethodResponse = super.remoteCore.act(publicKey, 
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
						accessMethodResponse = super.remoteCore.subscribe(publicKey,
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
						accessMethodResponse = super.remoteCore.unsubscribe(publicKey, 
							uriElements[1],uriElements[3],uriElements[5], 
							   subscriptionId);
					break;
					default: break;
				}
			default:
				break;
		}
		if(accessMethodResponse != null)
		{
			response = accessMethodResponse.toString();
		}
		return new JSONObject().put("uuid",
				uuid).put("response", response);
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
			String response = this.client.request(
					new JSONObject().put("uri", "/namespace"));
			if(response != null && response.length() > 0)
			{
				this.remoteNamespace = response;
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
		String response = this.client.request(
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
			super.getLocalNamespace(), this.namespace());
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint#doConnect()
	 */
	@Override
	public boolean doConnect() 
	{
		if(this.remoteNamespace!=null)
		{
			return true;
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
				return false;
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
						SocketEndpoint.this.client =
						new ClientSocketThread(mediator, SocketEndpoint.this,
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
					mediator.error(e);
					if(SocketEndpoint.this.client!=null 
							&& !SocketEndpoint.this.client.running())
					{
						SocketEndpoint.this.client = null;
					}
				}
			}
		}, 0, 1000*5);		
		int timeout = 60*3000;
		
		while(true)
		{
			if(timeout<=0 || remoteNamespace != null)
			{
				break;
			}
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
			return false;
		}
		return true;
	}

	/**
	 * 
	 */
	protected void serverStopped()
	{
		if(super.connected)
		{
			super.disconnect();
			
		} else
		{
			mediator.warn(
			"An error occurred that impeded the server start");
		}
	}

	/**
	 * 
	 */
	public void clientDisconnected()
	{
		if(super.connected)
		{
			super.disconnect();
			
		} else
		{
			mediator.warn(
			"An error occurred that impeded the client connection");
		}
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
		
		String response = this.client.request(
			new JSONObject(
				).put("uri", uri
				).put("agent", new JSONObject(
					).put("agentKey", agentKey
				    ).put("filter", filter==null?null:filter.toJSONObject())));

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
		
		String response = this.client.request(
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
		
		JSONObject object = new JSONObject(message.getJSON());
		String path = (String) object.remove("uri");
		
		object.put("uri", new StringBuilder().append("/"
			).append(super.localNamespace).append(":").append(
				path.substring(1)).toString());
		
		String response = this.client.request(
			new JSONObject().put("uri", uri
				).put("message", object));

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
		String response = this.client.request(
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
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getAll(java.lang.String)
	 */
	@Override
	public String getAll(String publicKey)
	{
		return this.getAll(publicKey, null);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#
	 * getAll(java.lang.String, java.lang.String)
	 */
	@Override
	public String getAll(String publicKey,String filter) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri = String.format("/all?%s",filter);

		String response = this.client.request(
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
	public String getProviders(String publicKey)
	{
		if(!super.connected)
		{
			return null;
		}
		String response = this.client.request(
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
	public String getProvider(String publicKey, 
			String serviceProviderId)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri = String.format("/providers/%s",serviceProviderId);

		String response = this.client.request(
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
	public String getServices(String publicKey, 
			String serviceProviderId)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services", serviceProviderId);

		String response = this.client.request(
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
	public String getService(String publicKey, 
			String serviceProviderId, String serviceId)
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s", 
				serviceProviderId,serviceId);

		String response = this.client.request(
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
	public String getResources(String publicKey, 
			String serviceProviderId, String serviceId) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources", 
				serviceProviderId,serviceId);

		String response = this.client.request(
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
	public String getResource(String publicKey, 
			String serviceProviderId, String serviceId, 
			String resourceId) 
	{
		if(!super.connected)
		{
			return null;
		}
		String uri =String.format("/providers/%s/services/%s/resources/%s", 
				serviceProviderId,serviceId,resourceId);

		String response = this.client.request(
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

		String response = this.client.request(
			new JSONObject(
					).put("uri", uri
					).put("pkey", publicKey));
		
		JSONObject result = null;
		if(response!=null)
		{
			mediator.debug(response.toString());
			try
			{
				result = new JSONObject(response);
			} catch(JSONException | NullPointerException e)
			{
				mediator.error(e);
			}
		}
		return result;
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
		
		Object value = JSONUtils.toJSONFormat(parameter);
		object = new JSONObject();		
		object.put("name", "arg0");
		object.put("type", value.getClass().getName());
		object.put("value", value);
		parameters.put(object);
		
		String response = this.client.request(
			new JSONObject(
			).put("uri", uri
			).put("pkey", publicKey
			).put("parameters", parameters));

		JSONObject result = null;
		if(response!=null)
		{
			mediator.debug(response.toString());
			try
			{
				result = new JSONObject(response);
			} catch(JSONException | NullPointerException e)
			{
				mediator.error(e);
			}
		}
		return result;
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
			Object value = JSONUtils.toJSONFormat(parameters[index]);
			JSONObject object = new JSONObject();		
			object.put("name", "arg"+index);
			object.put("type", value.getClass().getName());
			object.put("value", value);
			parametersArray.put(object);
		}		
		String response = this.client.request(
				new JSONObject(
				).put("uri", uri
			    ).put("pkey", publicKey
				).put("parameters", parametersArray));

		JSONObject result = null;
		if(response!=null)
		{
			mediator.debug(response.toString());
			try
			{
				result = new JSONObject(response);
			} catch(JSONException | NullPointerException e)
			{
				mediator.error(e);
			}
		}
		return result;
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

		String response = this.client.request(
				new JSONObject(
			    ).put("uri", uri
			    ).put("pkey", publicKey
				).put("parameters", parametersArray));

		JSONObject result = null;
		if(response!=null)
		{
			mediator.debug(response.toString());
			try
			{
				result = new JSONObject(response);
			} catch(JSONException | NullPointerException e)
			{
				mediator.error(e);
			}
		}
		return result;
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

		String response = this.client.request(
				new JSONObject(
			    ).put("uri", uri
				).put("pkey", publicKey
				).put("parameters", parametersArray));

		JSONObject result = null;
		if(response!=null)
		{
			mediator.debug(response.toString());
			try
			{
				result = new JSONObject(response);
			} catch(JSONException | NullPointerException e)
			{
				mediator.error(e);
			}
		}
		return result;
	}
}
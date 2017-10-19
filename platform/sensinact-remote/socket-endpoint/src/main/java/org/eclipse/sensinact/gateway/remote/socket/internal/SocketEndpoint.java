package org.eclipse.sensinact.gateway.remote.socket.internal;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaCallback;
import org.eclipse.sensinact.gateway.core.message.SnaCallback.Type;
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
	
	private Timer connectionTimer;
	TimerTask connectionTask  = new TimerTask()
	{
		@Override
		public void run()
		{			
			try 
			{
				if(SocketEndpoint.this.client == null)
				{
					SocketEndpoint.this.client = new ClientSocketThread(mediator);
					new Thread(SocketEndpoint.this.client).start();
					SocketEndpoint.this.mediator.debug("Client socket thread started");
				}
				if(SocketEndpoint.this.remoteNamespace == null)
				{
					SocketEndpoint.this.namespace();
					if(SocketEndpoint.this.remoteNamespace!=null)
					{ 
						mediator.debug("Connected remote namespace : "+
								SocketEndpoint.this.remoteNamespace);
						SocketEndpoint.this.mediator.debug(
								"Client socket thread connected");
						SocketEndpoint.this.connectionTimer.cancel();
					}
				}
			} catch (Exception e)
			{
				SocketEndpoint.this.mediator.error(e.getMessage(),e);
				if(SocketEndpoint.this.client!=null 
						&& !SocketEndpoint.this.client.running())
				{
					SocketEndpoint.this.client = null;
				}
			}
		}
	};
	
	/**
	 * @param mediator
	 * @param remoteCore
	 * @param localNamespace
	 * @throws IOException
	 */
	public SocketEndpoint(Mediator mediator,String localNamespace) 
	{
		super(mediator, localNamespace);
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
			case 0:
				if("/".equals(uri))
				{
					response = super.remoteCore.getProviders(publicKey);
				}
				break;
			case 1:			
				String[] subUriELements = uriElements[0].split("?");
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
						String agentId = subUriELements[0];
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
							JSONObject f = object.getJSONObject("filter");
							
							SnaFilter filter = new SnaFilter(mediator,
							    f.getString("sender"),
								f.getBoolean("pattern"), 
								f.getBoolean("complement"),
								f.getJSONArray("conditions"));
							
							JSONArray t = object.getJSONArray("types");
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
						String callbackId = subUriELements[0];
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
					default:
						response = super.remoteCore.getProvider(publicKey,
							uriElements[0]);
					break;
				}
				break;
			case 2:
				response = super.remoteCore.getServices(publicKey,
						uriElements[0]);
				break;
			case 3:
				response = super.remoteCore.getService(publicKey, 
						uriElements[0],uriElements[2]);
				break;
			case 4:
				response = super.remoteCore.getResources(publicKey,
						uriElements[0],uriElements[2]);
				break;
			case 5:
				response = super.remoteCore.getResource(publicKey, 
						uriElements[0],uriElements[2],uriElements[4]);
				break;
			case 6:
				switch(uriElements[5])
				{
					case "GET":				
					    response = super.remoteCore.get(publicKey, 
							uriElements[0],uriElements[2],uriElements[4], null);
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
								uriElements[0],uriElements[2],uriElements[4],
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
								uriElements[0],uriElements[2],
								uriElements[4], args);
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
							uriElements[0],uriElements[2], uriElements[4], 
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
							uriElements[0],uriElements[2], uriElements[4], 
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
	 * @see fr.cea.sna.gate way.core.RemoteEndpoint#namespace()
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

		if(response!=null)
		{
			mediator.debug(response.toString());
		}
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.Recipient#
	 * getSnaCallBackType()
	 */
	@Override
	public Type getSnaCallBackType() 
	{
		return SnaCallback.Type.UNARY;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.Recipient#
	 * getLifetime()
	 */
	@Override
	public long getLifetime() 
	{
		return SnaCallback.ENDLESS;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.Recipient#getBufferSize()
	 */
	@Override
	public int getBufferSize() 
	{
		return 0;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.Recipient#getSchedulerDelay()
	 */
	@Override
	public int getSchedulerDelay()
	{
		return 0;
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
	protected void doConnect() 
	{
		if(this.server!=null)
		{
			try
			{
				this.server = new ServerSocketThread(mediator, 
						this);				
				new Thread(server).start();
				
				this.connectionTimer = new Timer();
				this.connectionTimer.schedule(this.connectionTask, 
						1000, 1000*10);
			}
			catch (IOException e)
			{
				mediator.error(e);
				super.connected = false;
				return;
			}
		}
		String remoteNamespace = null;
		int timeout = 60*1000;
		
		while(timeout > 0
		&& (remoteNamespace = this.namespace())== null)
		{
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
		super.connected = (remoteNamespace != null);
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
					).put("uri", "/"
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
		String uri = String.format("/%s",serviceProviderId);

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
		String uri =String.format("/%s/services", serviceProviderId);

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
		String uri =String.format("/%s/services/%s", 
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
		String uri =String.format("/%s/services/%s/resources", 
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
		String uri =String.format("/%s/services/%s/resources/%s", 
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
		String uri =String.format("/%s/services/%s/resources/%s/GET", 
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
		String uri =String.format("/%s/services/%s/resources/%s/SET", 
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
		String uri =String.format("/%s/services/%s/resources/%s/ACT", 
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
		String uri =String.format("/%s/services/%s/resources/%s/UNSUBSCRIBE", 
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
		String uri =String.format("/%s/services/%s/resources/%s/SUBSCRIBE", 
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
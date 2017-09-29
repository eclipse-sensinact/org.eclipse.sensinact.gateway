package org.eclipse.sensinact.gateway.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Abstract implementation of a {@link RemoteEndpoint} service. 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractRemoteEndpoint 
extends AbstractStackEngineHandler<SnaMessage<?>> implements RemoteEndpoint
{

	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	/**
	 * Transmits the subscription request to the remote connected 
	 * {@link RemoteEndpoint} 
	 *  
	 * @param serviceProviderId the service provider String identifier
	 * @param serviceId the service String identifier
	 * @param resourceId the resource String identifier
	 * @param conditions the Json formated array of constraints applying
	 * on the subscription to be transmitted
	 *  
	 * @return the Json formated subscription response
	 */
	protected abstract JSONObject doSubscribe(String publicKey, 
		String serviceProviderId, String serviceId, String resourceId, 
		JSONArray conditions);

	/**
	 * Connects this {@link RemoteEndpoint} with the one held by 
	 * the remote sensiNact gateway instance to be connected to 
	 */
	protected abstract void doConnect();
	
	/**
	 * Disconnects this {@link RemoteEndpoint} from the one held by 
	 * the remote sensiNact gateway instance it is connected to
	 */
	protected abstract void doDisconnect();

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	protected final Mediator mediator;
	protected final RemoteCore remoteCore;
	protected final String localNamespace;
	
	protected boolean connected;	
	protected Map<String, Recipient> recipients;
	protected Executable<String, Void> onConnectedCallback;
	protected Executable<String, Void> onDisconnectedCallback;
	 
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link 
	 * RemoteEndpoint} to be instantiated to interact with the 
	 * OSGi host environment
	 * @param remoteCore the {@link RemoteCore} connected to the local 
	 * sensiNact gateway instance
	 * @param localNamespace the String namespace of the local sensiNact
	 * gateway instance
	 */
	public AbstractRemoteEndpoint(Mediator mediator, 
			RemoteCore remoteCore, String localNamespace)
	{
		this.remoteCore = remoteCore;
		this.mediator = mediator;
		this.recipients = new HashMap<String,Recipient>();
		this.connected = false;
		this.localNamespace = localNamespace;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
	 * onConnected(org.eclipse.sensinact.gateway.common.execution.Executable)
	 */
	public void onConnected(Executable<String, Void> onConnectedCallback)
	{
		this.onConnectedCallback = onConnectedCallback;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
	 * onDisconnected(org.eclipse.sensinact.gateway.common.execution.Executable)
	 */
	public void onDisconnected(Executable<String, Void> onDisconnectedCallback)
	{
		this.onDisconnectedCallback = onDisconnectedCallback;
	}	
	
	/**
	 * @inheritDoc
	 *
	 * @see fr.cea.sna.gateway.core.model.message.Recipient#
	 * callback(java.lang.String, fr.cea.sna.gateway.core.message.SnaMessage[])
	 */
	public void callback(String callbackId, SnaMessage[] messages) throws Exception
	{
		Recipient recipient = this.recipients.get(callbackId);
		if(recipient != null)
		{
			recipient.callback(callbackId, messages);
		}
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see fr.cea.sna.gateway.core.model.message.MessageRegisterer#
	 * createRemoteCore(fr.cea.sna.gateway.core.message.SnaMessage)
	 */
	public void register(SnaMessage<?> message)
	{
		super.eventEngine.push(message);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#disconnect()
	 */
	@Override
	public void disconnect()
	{
		if(!this.connected)
		{
			return;
		}
		this.connected = false;
		this.doDisconnect();
		if(this.onDisconnectedCallback!=null)
		{
			try 
			{
				this.onDisconnectedCallback.execute(
						this.namespace());
				
			} catch (Exception e)
			{
				mediator.error(e.getMessage(),e);
			}
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see fr.cea.sna.gateway.core.RemoteEndpoint#connect()
	 */
	@Override
	public void connect() 
	{
		if(this.connected)
		{
			mediator.debug("Endpoint already connected");
			return;
		}
		this.connected = true;
		this.doConnect();
		if(this.onConnectedCallback!=null)
		{
			try 
			{
				this.onConnectedCallback.execute(this.namespace());
				
			} catch (Exception e)
			{
				mediator.error(e.getMessage(),e);
			}
		}
	}   

	/**
	 * @inheritDoc
	 *
	 * @see fr.cea.sna.gateway.core.RemoteEndpoint#
	 * jsonSubscribe(java.lang.String, java.lang.String, java.lang.String, 
	 * fr.cea.sna.gateway.core.model.message.Recipient, org.json.JSONArray)
	 */
	@Override
	public JSONObject subscribe(String publicKey, 
			String serviceProviderId, String serviceId,
	        String resourceId, Recipient recipient, JSONArray conditions)
	{
		JSONObject response = this.doSubscribe(publicKey, 
				serviceProviderId, serviceId, 
				resourceId, conditions);
		try
		{
			this.recipients.put(response.getJSONObject("response"
				).getString("subscriptionId"),recipient);
			
		} catch(Exception e)
		{
			mediator.error(e.getMessage(),e);
		}
		return response;
	}
	
	/**
	 * Returns the String namespace of the local instance of
	 * the sensiNact gateway this {@link RemoteEndpoint} is 
	 * connected to, through the {@link RemoteCore}
	 * 
	 * @return the local sensiNact gateway's String namespace 
	 */
	protected String getLocalNamespace()
	{
		return this.localNamespace;
	}
}

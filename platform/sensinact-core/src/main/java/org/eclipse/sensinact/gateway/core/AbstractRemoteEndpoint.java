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
package org.eclipse.sensinact.gateway.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.core.security.Sessions.SessionObserver;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Abstract implementation of a {@link RemoteEndpoint} service. 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractRemoteEndpoint 
implements RemoteEndpoint, SessionObserver
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
	 * @param publicKey the String public key of the {@link Session} requiring
	 * for the subscription to be created
	 * @param serviceProviderId the service provider String identifier
	 * @param serviceId the service String identifier
	 * @param resourceId the resource String identifier
	 * @param conditions the JSON formated list of constraints applying
	 * on the subscription to be created
	 *  
	 * @return the JSON formated subscription response
	 */
	protected abstract JSONObject doSubscribe(String publicKey, 
		String serviceProviderId, String serviceId, String resourceId, 
		JSONArray conditions);

	/**
	 * Asks for the close of the remote {@link Session} whose String public
	 * key is passed as parameter
	 *  
	 * @param publicKey the String public key of the remote {@link Session} 
	 * to be closed
	 */
	protected abstract void closeSession(String publicKey);

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
	protected final String localNamespace;
	
	protected RemoteCore remoteCore;
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
	public AbstractRemoteEndpoint(Mediator mediator, String localNamespace)
	{
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
	public boolean connect(RemoteCore remoteCore) 
	{
		if(this.connected)
		{
			mediator.debug("Endpoint already connected");
			return true;
		}
		this.connected = true;
		this.remoteCore = remoteCore;
		this.doConnect();
		
		if(!this.connected)
		{
			mediator.debug("Endpoint is not connected");
			return false;
		}
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
		return true;
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
		if(!this.connected)
		{
			return null;
		}
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
	 * Returns the String namespace of the local sensiNact instance 
	 * this {@link RemoteEndpoint} is connected to, through the {@link 
	 * RemoteCore}
	 * 
	 * @return the local sensiNact gateway's String namespace 
	 */
	protected String getLocalNamespace()
	{
		return this.localNamespace;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.Sessions.SessionObserver#
	 * disappearing(java.lang.String)
	 */
	public void disappearing(String publicKey)
	{
		if(this.connected)
		{
			this.closeSession(publicKey);
		}
	}
}


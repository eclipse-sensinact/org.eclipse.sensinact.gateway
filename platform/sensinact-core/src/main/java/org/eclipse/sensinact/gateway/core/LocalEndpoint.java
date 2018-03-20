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

import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Extended {@link Endpoint} in charge of connecting a {@link RemoteCore} 
 * to the local sensiNact instance's {@link Core}
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
abstract class LocalEndpoint implements Endpoint
{
	/**
	 * Returns the {@link Session} for the user's String public 
	 * key passed as parameter
	 *  
	 * @param publicKey the user's String public key
	 * 
	 * @return the {@link Session} for the specified user
	 */
	abstract Session getSession(String publicKey);
	
	/**
	 * Closes the {@link Session} of the user whose 
	 * String public key passed as parameter
	 *  
	 * @param publicKey the user's String public key
	 */
	abstract void closeSession(String publicKey);
	
	/**
	 * Closes this local endpoint
	 */
	abstract void close(); 
	
	private final int localID;

	/**
	 * Constructor
	 * 
	 * @param localID this integer identifier
	 * of the LocalEndpoint to be instantiated
	 */
	LocalEndpoint(int localID)
	{
		this.localID = localID;
	}
	
	/**
	 * Returns the integer identifier of this LocalEndpoint
	 * 
	 * @return this LocalEndpoint's integer identifier
	 */
	public int localID()
	{
		return this.localID;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getAll(java.lang.String)
	 */
	@Override
	public String getAll(String publicKey)
	{
		return this.getSession(publicKey).getAll(
				).getResponse();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getAll(java.lang.String, java.lang.String)
	 */
	@Override
	public String getAll(String publicKey, String filter)
	{
		return this.getSession(publicKey).getAll(
				filter, null).getResponse();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getProviders(java.lang.String)
	 */
	@Override
	public String getProviders(String publicKey)
	{
		return this.getSession(publicKey).getProviders(
				).getResponse();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getProvider(java.lang.String, java.lang.String)
	 */
	@Override
	public String getProvider(String publicKey,
	        String serviceProviderId)
	{
		return this.getSession(publicKey).getProvider(
				serviceProviderId).getJSON();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getServices(java.lang.String, java.lang.String)
	 */
	@Override
	public String getServices(String publicKey,
	        String serviceProviderId)
	{
		return this.getSession(publicKey).getServices(
				serviceProviderId).getResponse();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getService(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getService(String publicKey, String serviceProviderId,
	        String serviceId)
	{
		return this.getSession(publicKey).getService(
			serviceProviderId, serviceId).getJSON();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getResources(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getResources(String publicKey,
	        String serviceProviderId, String serviceId)
	{
		return this.getSession(publicKey).getResources(
			serviceProviderId, serviceId).getResponse();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getResource(String publicKey,
	        String serviceProviderId, String serviceId, String resourceId)
	{
		return this.getSession(publicKey).getResource(
			serviceProviderId, serviceId, resourceId
			).getJSON();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject get(String publicKey, String serviceProviderId,
	        String serviceId, String resourceId, String attributeId)
	{
		return new JSONObject(
			this.getSession(publicKey).get(serviceProviderId,
			serviceId, resourceId, attributeId).getJSON());
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#set(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public JSONObject set(String publicKey, String serviceProviderId,
	        String serviceId, String resourceId, String attributeId,
	        Object parameter)
	{
		return new JSONObject(
			this.getSession(publicKey).set(serviceProviderId,
			serviceId, resourceId, attributeId, 
			parameter).getJSON());
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#act(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	@Override
	public JSONObject act(String publicKey, String serviceProviderId,
	        String serviceId, String resourceId, Object[] parameters)
	{
		return new JSONObject(
			this.getSession(publicKey).act(serviceProviderId,
			serviceId, resourceId, parameters).getJSON());
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#subscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.eclipse.sensinact.gateway.core.message.Recipient, org.json.JSONArray)
	 */
	@Override
	public JSONObject subscribe(String publicKey, String serviceProviderId,
	        String serviceId, String resourceId, Recipient recipient,
	        JSONArray conditions)
	{
		return new JSONObject(
			this.getSession(publicKey).subscribe(serviceProviderId,
			serviceId, resourceId, recipient, conditions
			).getJSON());
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#unsubscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject unsubscribe(String publicKey,
	        String serviceProviderId, String serviceId, String resourceId,
	        String subscriptionId)
	{
		return new JSONObject(
			this.getSession(publicKey).unsubscribe(serviceProviderId,
			serviceId, resourceId, subscriptionId
			).getJSON());
	}
}
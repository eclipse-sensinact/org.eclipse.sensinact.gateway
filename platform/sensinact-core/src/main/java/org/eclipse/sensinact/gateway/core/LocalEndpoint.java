package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Extended {@link Endpoint} in charge of realizing and maintaining the 
 * connection between a registered {@link RemoteCore} and the {@link Core} 
 * of the sensiNact instance it is connected to 
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
abstract class LocalEndpoint implements Endpoint
{

	/**
	 * @param publicKey
	 * @return
	 */
	abstract Session getSession(String publicKey);
	
	
	private final int localID;

	/**
	 * Constructor
	 * 
	 * @param localID
	 */
	LocalEndpoint(int localID)
	{
		this.localID = localID;
	}
	
	/**
	 * @return
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
	public JSONObject getAll(String publicKey)
	{
		return this.getSession(publicKey).getAll();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getAll(java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getAll(String publicKey, String filter)
	{
		return this.getSession(publicKey).getAll(filter);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getLocations(java.lang.String)
	 */
	@Override
	public JSONObject getLocations(String publicKey)
	{
		return this.getSession(publicKey).getLocations();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getProviders(java.lang.String)
	 */
	@Override
	public JSONObject getProviders(String publicKey)
	{
		return this.getSession(publicKey).getProviders();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getProvider(java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getProvider(String publicKey,
	        String serviceProviderId)
	{
		return this.getSession(publicKey).getProvider(serviceProviderId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getServices(java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getServices(String publicKey,
	        String serviceProviderId)
	{
		return this.getSession(publicKey).getServices(serviceProviderId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getService(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getService(String publicKey, String serviceProviderId,
	        String serviceId)
	{
		return this.getSession(publicKey).getService(serviceProviderId,
				serviceId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getResources(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getResources(String publicKey,
	        String serviceProviderId, String serviceId)
	{
		return this.getSession(publicKey).getResources(serviceProviderId,
				serviceId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Endpoint#getResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public JSONObject getResource(String publicKey,
	        String serviceProviderId, String serviceId, String resourceId)
	{
		return this.getSession(publicKey).getResource(serviceProviderId,
				serviceId, resourceId);
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
		return this.getSession(publicKey).get(serviceProviderId,
				serviceId, resourceId, attributeId);
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
		return this.getSession(publicKey).set(serviceProviderId,
				serviceId, resourceId, attributeId, parameter);
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
		return this.getSession(publicKey).act(serviceProviderId,
				serviceId, resourceId, parameters);
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
		return this.getSession(publicKey).subscribe(serviceProviderId,
				serviceId, resourceId, recipient, conditions);
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
		return this.getSession(publicKey).unsubscribe(serviceProviderId,
				serviceId, resourceId, subscriptionId);
	}
}
/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.remote;

import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Endpoint;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Extended {@link Endpoint} in charge of connecting a {@link RemoteCore} to the
 * local sensiNact instance's {@link Core}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class LocalEndpoint implements Endpoint {
	/**
	 * Returns the String namespace of the local sensiNact instance this
	 * LocalEndpoint is connected to
	 * 
	 * @return the connected local sensiNact's String namespace
	 */
	public abstract String localNamespace();

	/**
	 * Returns the {@link Session} for the user's String public key passed as
	 * parameter
	 * 
	 * @param publicKey
	 *            the user's String public key
	 * 
	 * @return the {@link Session} for the specified user
	 */
	public abstract Session getSession(String publicKey);

	/**
	 * Closes the {@link Session} of the user whose String public key passed as
	 * parameter
	 * 
	 * @param publicKey
	 *            the user's String public key
	 */
	public abstract void closeSession(String publicKey);

	/**
	 * @param localAgentId
	 */
	public abstract void unregisterAgent(String localAgentId);
	
	/**
	 * Closes this local endpoint
	 */
	public abstract void close();

	private final int localID;

	/**
	 * Constructor
	 * 
	 * @param localID
	 *            this integer identifier of the LocalEndpoint to be instantiated
	 */
	public LocalEndpoint(int localID) {
		this.localID = localID;
	}

	/**
	 * Returns the integer identifier of this LocalEndpoint
	 * 
	 * @return this LocalEndpoint's integer identifier
	 */
	public int localID() {
		return this.localID;
	}

	@Override
	public String getAll(String publicKey) {
		return this.getSession(publicKey).getAll().getResponse();
	}

	@Override
	public String getAll(String publicKey, String filter) {
		return this.getSession(publicKey).getAll(filter, null).getResponse();
	}

	@Override
	public String getProviders(String publicKey) {
		return this.getSession(publicKey).getProviders().getResponse();
	}

	@Override
	public String getProvider(String publicKey, String serviceProviderId) {
		return this.getSession(publicKey).getProvider(serviceProviderId).getJSON();
	}

	@Override
	public String getServices(String publicKey, String serviceProviderId) {
		return this.getSession(publicKey).getServices(serviceProviderId).getResponse();
	}

	@Override
	public String getService(String publicKey, String serviceProviderId, String serviceId) {
		return this.getSession(publicKey).getService(serviceProviderId, serviceId).getJSON();
	}

	@Override
	public String getResources(String publicKey, String serviceProviderId, String serviceId) {
		return this.getSession(publicKey).getResources(serviceProviderId, serviceId).getResponse();
	}

	@Override
	public String getResource(String publicKey, String serviceProviderId, String serviceId, String resourceId) {
		return this.getSession(publicKey).getResource(serviceProviderId, serviceId, resourceId).getJSON();
	}

	@Override
	public JSONObject get(String publicKey, String serviceProviderId, String serviceId, String resourceId,String attributeId) {
		return new JSONObject(this.getSession(publicKey).get(serviceProviderId, serviceId, resourceId, attributeId).getJSON());
	}

	@Override
	public JSONObject set(String publicKey, String serviceProviderId, String serviceId, String resourceId,String attributeId, Object parameter) {
		return new JSONObject(this.getSession(publicKey).set(serviceProviderId, serviceId, resourceId, attributeId, parameter).getJSON());
	}

	@Override
	public JSONObject act(String publicKey, String serviceProviderId, String serviceId, String resourceId,Object[] parameters) {
		return new JSONObject(this.getSession(publicKey).act(serviceProviderId, serviceId, resourceId, parameters).getJSON());
	}

	@Override
	public JSONObject subscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId,Recipient recipient, JSONArray conditions) {
		return new JSONObject(this.getSession(publicKey).subscribe(serviceProviderId, serviceId, resourceId, recipient, conditions).getJSON());
	}

	@Override
	public JSONObject unsubscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId,String subscriptionId) {
		return new JSONObject(this.getSession(publicKey).unsubscribe(serviceProviderId, serviceId, resourceId, subscriptionId).getJSON());
	}
}
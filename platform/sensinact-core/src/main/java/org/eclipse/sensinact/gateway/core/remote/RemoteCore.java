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
package org.eclipse.sensinact.gateway.core.remote;

import java.util.Collection;

import org.eclipse.sensinact.gateway.api.core.Endpoint;
import org.eclipse.sensinact.gateway.api.message.SnaMessage;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.MessageFilter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A RemoteCore represents a remote instance of sensiNact to which the local one
 * is connected to. It is linked to the local instance of sensiNact by the way
 * of a {@link LocalEndpoint}, and it is linked to the remote instance of
 * sensiNact using a {@link RemoteEndpoint}. The RemoteCore is in charge,
 * between others, of maintaining the set of {@link Session}s instantiated
 * because of the accesses to the local sensiNact instance from the remote
 * connected one. It is also in charge of deleting them when their remote
 * counterpart disappear
 * <p/>
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface RemoteCore extends Endpoint {
	/**
	 * Returns the {@link RemoteEndpoint} attached to this RemoteCore
	 * 
	 * @return this RemoteCore's {@link RemoteEndpoint}
	 */
	RemoteEndpoint endpoint();

	/**
	 * Opens a connection with a remote sensiNact instance by the way of the
	 * {@link RemoteEndpoint} passed as parameter
	 * 
	 * @param endpoint
	 *            the {@link RemoteEndpoint} allowing this RemoteCore to connect to
	 *            a remote sensiNact instance
	 */
	void open(RemoteEndpoint endpoint);

	/**
	 * Closes the connection with the remote sensiNact instance
	 */
	void close();

	/**
	 * Registers this RemoteCore to the local instance of sensiNact and associates
	 * it with the specified namespace
	 * 
	 * @param namespace
	 *            the connected remote sensiNact instance's String namespace
	 */
	void connect(String namespace);

	/**
	 * Unregisters this RemoteCore from the local instance of sensiNact
	 */
	void disconnect();

	/**
	 * Returns the unique integer identifier of this RemoteCore
	 * 
	 * @return this RemoteCore's integer identifier
	 */
	int localID();

	/**
	 * Returns the String namespace of the local instance of sensiNact this
	 * RemoteCore is connected to.
	 * 
	 * @return this local instance of sensiNact's String namespace
	 */
	String namespace();

	/**
	 * Registers the Collection of {@link Executable}s to be triggered when the
	 * {@link RemoteEndpoint} of this RemoteCore is connected
	 * 
	 * @param onConnectedCallbacks
	 *            the Collection of {@link Executables} to be executed at connection
	 *            time
	 */
	void onConnected(Collection<Executable<String, Void>> onConnectedCallbacks);

	/**
	 * Registers the Collection of {@link Executable}s to be triggered when the
	 * {@link RemoteEndpoint} of this RemoteCore is disconnected
	 * 
	 * @param onDisconnectedCallbacks
	 *            the Collection of {@link Executable}s to be executed at
	 *            disconnection time
	 */
	void onDisconnected(Collection<Executable<String, Void>> onDisconnectedCallbacks);

	/**
	 * Relays a subscription called from the remote connected instance of sensiNact
	 * 
	 * @param publicKey
	 *            the subscription invoker's String public key
	 * @param serviceProviderId
	 *            the String identifier of the service provider holding the service
	 *            providing the resource on which the subscription applies
	 * @param serviceId
	 *            the String identifier of the service providing the resource on
	 *            which the subscription applies
	 * @param resourceId
	 *            the String identifier of the resource on which the subscription
	 *            applies
	 * @param conditions
	 *            the JSON array formated set of the constraints applying on the
	 *            subscription
	 * @return the JSON formated response of the subscription access method
	 *         invocation
	 */
	JSONObject subscribe(String publicKey, String serviceProviderId, String serviceId, 
		String resourceId, JSONArray conditions);

	/**
	 * Registers an {@link SnaAgent} in the local instance of sensiNact. The
	 * {@link SnaAgent} links to a remote one (in the remote connected sensiNact
	 * instance) to witch it will dispatch the {@link SnaMessage}s
	 * 
	 * @param identifier
	 *            the String identifier of the remote related {@link SnaAgent}
	 * @param filter
	 *            the {@link MessageFilter} applying on the remote related
	 *            {@link SnaAgent}
	 * @param agentKey
	 *            the public key of the remote related {@link SnaAgent}
	 */
	void registerAgent(String identifier, MessageFilter filter, String agentKey);

	/**
	 * Unregisters the {@link SnaAgent} whose identifier is passed as parameter
	 * 
	 * @param identifier
	 *            the String identifier of the {@link SnaAgent} to be unregistered
	 */
	void unregisterAgent(String identifier);

	/**
	 * Relays the {@link SnaMessage} passed as parameter to the local
	 * {@link SnaAgent} whose identifier is also passed as parameter
	 * 
	 * @param agentId
	 *            the String identifier of the {@link SnaAgent} to which relay the
	 *            event message
	 * @param message
	 *            the {@link SnaMessage} to be relayed
	 */
	void dispatch(String agentId, SnaMessage<?> message);

	/**
	 * Closes the {@link Session} of the user whose public key is passed as
	 * parameter, because of its remote counterpart disappearance.
	 * 
	 * @param publicKey
	 *            the String public key of the user
	 */
	void closeSession(String publicKey);
}

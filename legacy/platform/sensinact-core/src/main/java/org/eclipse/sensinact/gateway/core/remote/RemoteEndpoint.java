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

import org.eclipse.sensinact.gateway.core.Endpoint;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;

/**
 * Extended {@link Endpoint} in charge of realizing and maintaining the
 * connection to another RemoteEndpoint provided by a remote instance of the
 * sensiNact gateway to be connected to
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface RemoteEndpoint extends Endpoint, Recipient {
	/**
	 * Attaches this RemoteEndpoint to the {@link RemoteCore} passed as parameter
	 * and initializes the connection to a RemoteEndpoint held by a remote sensiNact
	 * instance
	 * 
	 * @param remoteCore
	 *            the {@link RemoteCore} to which this RemoteEndpoint is attached
	 *            and thanks to which it is connected to the local sensiNact
	 *            instance
	 */
	void open(RemoteCore remoteCore);

	/**
	 * Disconnects this RemoteEndpoint from the one provided by the remote sensiNact
	 * instance it is connected to
	 */
	void close();

	/**
	 * Returns the namespace of the remotely connected sensiNact instance
	 * 
	 * @return the namespace of the remotely connected sensiNact
	 */
	String namespace();

	/**
	 * Registers a {@link SnaAgent} with the String identifier is passed as
	 * parameter to the remote sensiNact instances this RemoteEndpoint is connected
	 * to
	 * 
	 * @param identifier
	 *            the String identifier of the {@link SnaAgent} to be registered
	 * @param filter
	 *            the {@link SnaFilter} allowing the {@link SnaAgent}s to be
	 *            registered to discriminate through the messages to be dispatched
	 *            or not
	 * @param agentKey
	 *            the public String key allowing to retrieve access rights of the
	 *            {@link SnaAgent}s to be registered
	 */
	void registerAgent(String identifier, SnaFilter filter, String agentKey);

	/**
	 * Unregisters the remote instances of the {@link SnaAgent} whose String
	 * identifier is passed as parameter from the remote sensiNact instances this
	 * RemoteEndpoint is connected to
	 * 
	 * @param identifier
	 *            the String identifier of the {@link SnaAgent} to be unregistered
	 */
	void unregisterAgent(String identifier);

	/**
	 * Dispatches the {@link SnaMessage} passed as parameter to the remote instances
	 * of sensiNact this RemoteEndpoint is connected to
	 * 
	 * @param agentId
	 *            the String identifier of the {@link SnaAgent} which transmits the
	 *            message
	 * @param message
	 *            the {@link SnaMessage} to be transmitted
	 */
	void dispatch(String agentId, SnaMessage<?> message);
}

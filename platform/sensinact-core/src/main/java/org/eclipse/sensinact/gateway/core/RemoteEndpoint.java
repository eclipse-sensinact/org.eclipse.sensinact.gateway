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
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;

/**
 * Extended {@link Endpoint} in charge of realizing and maintaining the 
 * connection to another RemoteEndpoint provided by a remote instance of 
 * the sensiNact gateway to be connected to
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface RemoteEndpoint extends Endpoint, Recipient
{
	/**
	 * Connects this RemoteEndpoint to one provided by a remote
	 * sensiNact instance 
	 * 
	 * @param remoteCore
	 * 
	 * @return
	 */
	boolean connect(RemoteCore remoteCore);

	/**
	 * Disconnects this RemoteEndpoint from the one provided by the remote
	 * sensiNact instance it is connected to
	 */
	void disconnect();
	
	/**
	 * Returns the namespace of the remotely connected sensiNact
	 * instance
	 * 
	 * @return the namespace of the remotely connected sensiNact
	 */
	String namespace();  
	
	/**
	 * @param identifier
	 * @param filter
	 * @param agentKey
	 */
	void registerAgent(String identifier, SnaFilter filter, String agentKey);

	/**
	 * @param identifier
	 */
	void unregisterAgent(String identifier);

	/**
	 * @param agentId
	 * @param message
	 */
	void dispatch(String agentId, SnaMessage<?> message);
}

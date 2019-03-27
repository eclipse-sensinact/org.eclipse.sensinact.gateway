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

import java.security.InvalidKeyException;
import java.util.Collection;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.api.Sensinact;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.remote.AbstractRemoteEndpoint;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;

/**
 * Core service of the sensiNact platform
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Core extends Sensinact {

	/**
	 * Namespace property key
	 */
	public static final String NAMESPACE_PROP = "namespace";

	/**
	 * Instantiates and registers a new agent, connected to the {@link MidAgentCallback} 
	 * and whose received messages are filtered by the {@link SnaFilter} passed as
	 * parameters 
	 * 
	 * @param mediator the {@link Mediator} allowing the agent to be instantiated 
	 * to interact with the OSGi host environment
	 * @param callback the {@link MidAgentCallback} in charge of handling the
	 * messages transmitted to the agent to be instantiated
	 * @param filter the {@link SnaFilter} helping in filtering the messages
	 * transmitted to the agent to be instantiated and registered
	 * 
	 * @return the String identifier of the newly created and registered agent
	 */
	String registerAgent(Mediator mediator, MidAgentCallback callback, SnaFilter filter);
	
	/**
	 * Instantiates and registers a new {@link ResourceIntent} targeting the 
	 * {@link Resource} whose String path is passed as parameter and executing the 
	 * {@link Executable} also passed as parameter the availability status of the target
	 * changes 
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link ResourceIntent} to be instantiated 
	 * to interact with the OSGi host environment
	 * @param path the String path of the targeted {@link Resource} 
	 * @param callback the {@link Executable} to be executing when the availability status 
	 * of the specified target changes
	 * @return the String identifier of the newly created and registered {@link ResourceIntent}
	 */
	String registerIntent(Mediator mediator, Executable<Boolean,Void> onAccessible, final String... path);
	
	/**
	 * Creates and returns a {@link Session} for the application whose private
	 * String identifier is passed as parameter.
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * @param privateKey
	 *            the application's private String identifier
	 * 
	 * @return the {@link Session} for the specified application
	 */
	Session getApplicationSession(Mediator mediator, String privateKey);

	/**
	 * Returns the {@link AnonymousSession} for the anonymous user
	 * 
	 * @return the anonymous user's {@link AnonymousSession}
	 */
	AnonymousSession getAnonymousSession();

	/**
	 * Returns the {@link AuthenticatedSession} whose String identifier is passed as parameter
	 * 
	 * @param token the String identifier of the {@link Session}
	 * 
	 * @return the {@link AuthenticatedSession} with the specified identifier
	 */
	AuthenticatedSession getSession(String token);

	/**
	 * Creates and returns a {@link Session} for the user whose
	 * {@link Authentication} instance is passed as parameter
	 * 
	 * @param authentication
	 *            a user's {@link Authentication}
	 * 
	 * @return the {@link Session} for the specified user
	 */
	Session getSession(Authentication<?> authentication) throws InvalidKeyException, 
	DataStoreException, InvalidCredentialException;
	
	/**
	 * Closes this Core
	 */
	void close();
}

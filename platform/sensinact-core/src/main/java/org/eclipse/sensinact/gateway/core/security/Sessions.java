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
package org.eclipse.sensinact.gateway.core.security;

import java.security.InvalidKeyException;

/**
 * A service handling a Collection of {@link Session}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Sessions
{       
	public static final ThreadLocal<Session> SESSIONS = new ThreadLocal<Session>();
			
	/**
	 * Returns if it exists the {@link Session} of the user
	 * whose unique Long identifier is passed as 
	 * parameter
	 * 
	 * @param userId the user identifier for
	 * which to retrieve the existing {@link Session}
	 * 
	 * @return the {@link Session} for the specified 
	 * Long user identifier
	 */
	Session get(long userId);
	
	/**
	 * Returns if it exists the {@link Session} of the user
	 * whose unique identifier is passed as parameter
	 * 
	 * @param token the string identifier of the 
	 * {@link Session} to retrieve
	 * 
	 * @return the {@link Session} for the specified 
	 * String token
	 */
	Session get(String token);
	
	/**
	 * Registers the {@link Session} passed as parameter
	 * to the list of those managed by this Sessions 
	 * service
	 * 
	 * @param session the {@link Session} to register
	 */
	void register(Session session);
	
	/**
	 * Generates and returns a valid String token
	 * to create a new {@link Session}
	 * 
	 * @return a valid String token to create a
	 * new {@link Session}
	 * 
	 * @throws InvalidKeyException 
	 */
	String nextToken() throws InvalidKeyException;
	
}

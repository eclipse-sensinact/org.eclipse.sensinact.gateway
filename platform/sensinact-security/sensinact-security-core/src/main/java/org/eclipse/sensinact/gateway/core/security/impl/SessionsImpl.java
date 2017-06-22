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
package org.eclipse.sensinact.gateway.core.security.impl;

import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.core.security.Sessions;
import org.eclipse.sensinact.gateway.util.CryptoUtils;

/**
 * An array of {@link Session}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
final class SessionsImpl implements Sessions
{       

	//********************************************************************//
	//						NESTED DECLARATIONS		    				  //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
	
	//********************************************************************//
	//						STATIC DECLARATIONS		      				  //
	//********************************************************************//


	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	private Map<Session.Key,Session> tokens = new HashMap<Session.Key,Session>();
	
	/**
     * @param token
     * @return
     */
    public Session get(String token) 
    {
    	Session session = null;
    	Session.Key key = new Session.Key();
    	key.setToken(token);

		synchronized(this.tokens)
		{
    		session = this.tokens.get(key);
		}
		return session;
    }
    
    /**
     * @inheritDoc
     * 
     * @see Sessions#get(long)
     */
    public Session get(long userId)
    {
    	Session session = null;
    	Session.Key key = new Session.Key();
    	key.setUid(userId);
		synchronized(this.tokens)
		{
			session = this.tokens.get(key);
    	}
		return session;
    }

    /**
     * @inheritDoc
     * 
	 * @see Sessions#
	 * register(Session)
	 */
	public void register(Session session) 
	{
		synchronized(this.tokens)
		{
			this.tokens.put(session.getSessionKey(), session);
		}
	}
	  
    /**
	 * @return
	 * @param securedAccessImpl TODO
	 * @throws InvalidKeyException
	 */
	public String nextToken() throws InvalidKeyException
	{	
		boolean exists = false;
		String token = null;
		do
		{
			token = CryptoUtils.createToken();

			synchronized(this.tokens)
			{	
				exists = this.tokens.get(token)!=null;
			}
		} while(exists);
		
		return token;		
	}
}

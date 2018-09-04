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

import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

/**
 * Extended {@link Session} dedicated to anonymous accesses  
 */
public interface AnonymousSession extends Session {
	/**
	 * Registers a new user whose login, password and account are
	 * passed as parameters. If a user already exists with the same 
	 * login or account an {@link SecuredAccessException} exception 
	 * is thrown 
	 * 
	 * @param login
	 *        the String name of the user to be created
	 * @param password
	 *        the MD5 hashed password of the user to be created
	 * @param account
	 *        the account endpoint of the user to be created
	 * @param accountType
	 *        the type of the account endpoint of the user to be created
	 *        
	 * @throws SecuredAccessException if a user already exists with the same
	 * login or account
	 */
	void registerUser(String login, String password, String account, String accountType)
	throws SecuredAccessException;

	/**
	 * Renews the password of the user whose account endpoint is 
	 * passed as parameter
	 * 
	 * @param account
	 *            the account endpoint of the user
	 *            
	 * @throws SecuredAccessException if no user with the specified 
	 * account exists 
	 */
	void renewPassword(String account) throws SecuredAccessException;;
}

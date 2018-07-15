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

/**
 *
 */
public interface AnonymousSession extends Session {
	/**
	 * Asks for a user registration in the system.
	 * 
	 * @param login
	 *            the String name of the user to be created
	 * @param password
	 *            the MD5 hashed password of the user to be created
	 * @param account
	 *            the email address of the user to be created
	 */
	void registerUser(String login, String password, String account);

	/**
	 * Asks for a password renewing, for the user whose email address is passed as
	 * parameter
	 * 
	 * @param email
	 *            the email address of the user whose password is to be renewed
	 */
	void renewPassword(String email);
}

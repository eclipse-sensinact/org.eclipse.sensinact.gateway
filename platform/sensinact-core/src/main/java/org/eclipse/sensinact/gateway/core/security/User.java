/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security;

/**
 * 
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface User {
	public static final String MAIL_ACCOUNT = "MAIL";
	public static final String TWITTER_ACCOUNT = "TWITTER";
	public static final String FACEBOOK_ACCOUNT = "FACEBOOK";

	/**
	 * Returns the String public key of this User
	 * 
	 * @return this User's String public key
	 */
	String getPublicKey();

	/**
	 * Returns the String account type name of this User account
	 * 
	 * @return this User's String account type name
	 */
	String getAccountType();

	/**
	 * Returns the String account endpoint of this User
	 * 
	 * @return this User's String account endpoint
	 */
	String getAccount();

	/**
	 * @return
	 */
	boolean isAnonymous();

}

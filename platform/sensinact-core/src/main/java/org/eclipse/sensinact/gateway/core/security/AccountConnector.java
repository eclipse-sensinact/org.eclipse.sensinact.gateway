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

/**
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface AccountConnector {

	public static final String ACCOUNT_TYPE_PROP="ACCOUNT_TYPE";
	
	/**
	 * Returns true if this AccountConnector handle the string type
	 * of account passed parameter; returns false otherwise
	 * 
	 * @param accountType the type of account
	 * @return 
	 * 		<ul>
	 * 			<li>true if this AccountConnector handles the specified
	 * 				account type</li>
	 * 			<li>false otherwise</li>
	 *		</ul>
	 */
	boolean handle(String accountType);

	/**
	 * Connects this account connector to the appropriate account provider
	 * to validate the operation held by the {@link UserUpdater} passed as 
	 * parameter using the String token also passed as parameter
	 * 
	 * @param token the String token allowing to validate the {@link UserUpdater}'s operation
	 * @param userUpdater the {@link UserUpdater} holding the operation to
	 * be validated
	 */
	void connect(String token, UserUpdater userUpdater);
}

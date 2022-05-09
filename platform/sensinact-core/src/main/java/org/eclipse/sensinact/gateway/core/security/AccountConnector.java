/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
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

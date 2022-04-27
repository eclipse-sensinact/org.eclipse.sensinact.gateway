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
 * A UserManagerFinalizer is in charge of completing user management processes : 
 * creation, update, deletion
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface UserManagerFinalizer {
	
	/**
	 * Completes the creation of a user.
	 * 
	 * @param login  the String login of the created user
	 * @param publicKey the String public key of the created user
	 * @param account the String account endpoint used to validate the creation request
	 * @param accountType the String account type of the account endpoint used to validate the creation request
	 */
	void userCreated(String login, String publicKey, String account, String accountType) 
			throws SecuredAccessException;

	/**
	 * Completes the update of a user.
	 * 
	 * @param login the String login of the updated user
	 * @param oldPublicKey the previous String public key of the updated user
	 * @param publicKey the String public key of the updated user
	 */
	void userUpdated(String login, String oldPublicKey, String publicKey ) 
			throws SecuredAccessException;
	
	/**
	 * Completes the deletion of a user.
	 * 
	 * @param login the String login of the deleted user
	 * @param publicKey the String public key of the deleted user
	 */
	void userDeleted(String login, String publicKey) 
			throws SecuredAccessException;

}

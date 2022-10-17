/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

/**
 * A Session allows to invoke access method on resources, and to access to
 * available service providers, services, and/or resources
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AuthenticatedSession extends Session {
	/**
	 * Allows to replace the current password of the user attached to this Session
	 * with the new one passed as parameter
	 * 
	 * @param oldPassword
	 *            the current String password to be replaced
	 * @param newPassword
	 *            the new String password to be set
	 * @throws SecuredAccessException 
	 */
	void changePassword(String oldPassword, String newPassword) throws SecuredAccessException;
}

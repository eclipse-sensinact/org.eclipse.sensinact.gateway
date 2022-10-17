/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.dao;

import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

/**
 * A DAO Exception wraps any exception of the underlying code, such as
 * SQLExceptions.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DAOException extends SecuredAccessException {
	/**
	 * Constructs a DAOException with the given detail message.
	 * 
	 * @param message
	 *            The detail message of the DAOException.
	 */
	public DAOException(String message) {
		super(message);
	}

	/**
	 * Constructs a DAOException with the given root cause.
	 * 
	 * @param cause
	 *            The root cause of the DAOException.
	 */
	public DAOException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a DAOException with the given detail message and root cause.
	 * 
	 * @param message
	 *            The detail message of the DAOException.
	 * @param cause
	 *            The root cause of the DAOException.
	 */
	public DAOException(String message, Throwable cause) {
		super(message, cause);
	}

}

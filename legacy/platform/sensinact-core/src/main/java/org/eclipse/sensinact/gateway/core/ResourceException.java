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

/**
 * Thrown to indicate a resource related exception.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an <code>ResourceException</code> with no detail message.
	 */
	public ResourceException() {
		super();
	}

	/**
	 * Constructs an <code>ResourceException</code> with the specified detail
	 * message.
	 * 
	 * @param message
	 *            the detail message
	 */
	public ResourceException(String message) {
		super(message);
	}

	/**
	 * Constructs an <code>ResourceException</code> with the specified detail
	 * message and cause.
	 * 
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause
	 */
	public ResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an <code>ResourceException</code> with the specified cause.
	 * 
	 * @param cause
	 *            the cause
	 */
	public ResourceException(Throwable cause) {
		super(cause);
	}

}

/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnknownAccessMethodResponse extends AccessMethodResponse<String> {

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * @param uri
	 * @param typeName
	 * @param status
	 */
	public UnknownAccessMethodResponse(String uri) {
		this( uri, Response.UNKNOWN_METHOD_RESPONSE, Status.ERROR, 404);
	}

	/**
	 * @param uri
	 * @param type
	 * @param status
	 * @param statusCode
	 */
	public UnknownAccessMethodResponse(String uri, Response type, Status status, int statusCode) {
		super(uri, type, status, statusCode);
	}

}

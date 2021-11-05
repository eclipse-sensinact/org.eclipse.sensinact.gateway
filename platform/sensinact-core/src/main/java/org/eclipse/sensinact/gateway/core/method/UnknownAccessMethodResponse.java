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

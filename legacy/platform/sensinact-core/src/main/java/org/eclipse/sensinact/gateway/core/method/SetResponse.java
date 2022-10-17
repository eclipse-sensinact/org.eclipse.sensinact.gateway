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

import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;

/**
 * Extended {@link AccessMethodJSONResponse} returned by an {@link SetMethod}
 * invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SetResponse extends AccessMethodJSONResponse {
	/**
	 * Constructor
	 * 
	 * @param status
	 *            the associated {@link Status}
	 */
	protected SetResponse(String uri, Status status) {
		this(uri, status,
				Status.SUCCESS.equals(status) ? SnaErrorfulMessage.NO_ERROR : SnaErrorfulMessage.UNKNOWN_ERROR_CODE);
	}

	/**
	 * Constructor
	 * 
	 * @param status
	 *            the associated {@link Status}
	 * @param code
	 *            the associated status code
	 */
	public SetResponse( String uri, Status status, int code) {
		super(uri, AccessMethodResponse.Response.SET_RESPONSE, status, code);
	}
}

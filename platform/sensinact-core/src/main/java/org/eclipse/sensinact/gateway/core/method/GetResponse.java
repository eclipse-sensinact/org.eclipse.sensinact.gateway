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

import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;

/**
 * Extended {@link AccessMethodJSONResponse} returned by an {@link GetMethod}
 * invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class GetResponse extends AccessMethodJSONResponse {
	/**
	 * Constructor
	 * 
	 * @param status
	 *            the associated {@link Status}
	 */
	public GetResponse(String uri, Status status) {
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
	public GetResponse(String uri, Status status, int code) {
		super(uri, AccessMethodResponse.Response.GET_RESPONSE, status, code);
	}
}

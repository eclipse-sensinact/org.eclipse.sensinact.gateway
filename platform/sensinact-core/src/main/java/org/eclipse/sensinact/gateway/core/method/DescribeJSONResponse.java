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
import org.json.JSONObject;

/**
 * Extended {@link AccessMethodResponse} returned by an {@link DescribeMethod}
 * invocation and holding a JSONObject as result object
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DescribeJSONResponse extends DescribeResponse<JSONObject> {
	/**
	 * Constructor
	 * 
	 * @param status
	 *            the associated {@link Status}
	 */
	protected DescribeJSONResponse(String uri, Status status,
			DescribeMethod.DescribeType describeType) {
		this(uri, status,
				Status.SUCCESS.equals(status) ? SnaErrorfulMessage.NO_ERROR : SnaErrorfulMessage.UNKNOWN_ERROR_CODE,
				describeType);
	}

	/**
	 * Constructor
	 * 
	 * @param status
	 *            the associated {@link Status}
	 * @param code
	 *            the associated status code
	 */
	public DescribeJSONResponse(String uri, Status status, int code,
			DescribeMethod.DescribeType describeType) {
		super(uri, status, code, describeType);
	}

}

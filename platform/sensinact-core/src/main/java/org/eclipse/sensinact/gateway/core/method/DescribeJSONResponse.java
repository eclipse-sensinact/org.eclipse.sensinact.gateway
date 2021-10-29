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

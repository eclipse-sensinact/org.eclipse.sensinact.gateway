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
package org.eclipse.sensinact.gateway.core.method.legacy;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
/**
 * Extended {@link AccessMethodResponse} returned by an {@link DescribeMethod}
 * invocation and holding a String as result object
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DescribeStringResponse extends DescribeResponse<String> {
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param uri
	 * @param status
	 * @param describeType
	 */
	public DescribeStringResponse(Mediator mediator, String uri, Status status,
			DescribeMethod.DescribeType describeType) {
		this(mediator, uri, status,
				Status.SUCCESS.equals(status) ? SnaErrorfulMessage.NO_ERROR : SnaErrorfulMessage.UNKNOWN_ERROR_CODE,
				describeType);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param uri
	 * @param status
	 * @param code
	 * @param describeType
	 */
	public DescribeStringResponse(Mediator mediator, String uri, Status status, int code,
			DescribeMethod.DescribeType describeType) {
		super(mediator, uri, status, code, describeType);
	}

}

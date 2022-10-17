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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Extended {@link AccessMethodResponseBuilder} dedicated to
 * {@link DescribeMethod} execution and {@link DescribeResponse} creation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public abstract class DescribeResponseBuilder<T> extends AccessMethodResponseBuilder<T, DescribeResponse<T>> {
	protected DescribeMethod.DescribeType describeType;
	protected boolean payloadOnly;

	/**
	 * @param uri
	 * @param describeType
	 */
	protected DescribeResponseBuilder(Mediator mediator, String uri, DescribeMethod.DescribeType describeType) {
		super(uri, null);
		this.describeType = describeType;
	}

	/**
	 * @param payloadOnly
	 * @return
	 */
	public DescribeResponseBuilder<T> withPayloadOnly(boolean payloadOnly) {
		this.payloadOnly = payloadOnly;
		return this;
	}
}

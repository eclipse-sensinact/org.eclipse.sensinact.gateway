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
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;

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
	 * @param mediator
	 * @param uri
	 * @param describeType
	 */
	protected DescribeResponseBuilder(Mediator mediator, String uri, DescribeMethod.DescribeType describeType) {
		super(mediator, uri, null);
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

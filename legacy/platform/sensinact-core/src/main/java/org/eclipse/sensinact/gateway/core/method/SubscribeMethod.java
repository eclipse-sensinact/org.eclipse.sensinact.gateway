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

import jakarta.json.JsonObject;

/**
 * Subscription {@link AccessMethod}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SubscribeMethod extends AbstractAccessMethod<JsonObject, SubscribeResponse> {
	/**
	 * Constructor
	 */
	public SubscribeMethod(Mediator mediator, String uri, AccessMethodExecutor preProcessingExecutor) {
		super(mediator, uri, AccessMethod.SUBSCRIBE, preProcessingExecutor);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AbstractAccessMethod#
	 *      createAccessMethodResponseBuilder(java.lang.Object[])
	 */
	@Override
	protected SubscribeResponseBuilder createAccessMethodResponseBuilder(Object[] parameters) {
		return new SubscribeResponseBuilder(uri, parameters);
	}
}

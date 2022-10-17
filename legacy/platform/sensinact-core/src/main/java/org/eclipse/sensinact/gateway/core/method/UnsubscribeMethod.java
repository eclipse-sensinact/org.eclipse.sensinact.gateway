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
 * Unsubscription {@link AccessMethod}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnsubscribeMethod extends AbstractAccessMethod<JsonObject, UnsubscribeResponse> {
	/**
	 * Constructor
	 */
	public UnsubscribeMethod(Mediator mediator, String uri, AccessMethodExecutor preProcessingExecutor) {
		super(mediator, uri, AccessMethod.UNSUBSCRIBE, preProcessingExecutor);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AbstractAccessMethod#
	 *      createAccessMethodResponseBuilder(java.lang.Object[])
	 */
	@Override
	protected UnsubscribeResponseBuilder createAccessMethodResponseBuilder(Object[] parameters) {
		return new UnsubscribeResponseBuilder(uri, parameters);
	}
}

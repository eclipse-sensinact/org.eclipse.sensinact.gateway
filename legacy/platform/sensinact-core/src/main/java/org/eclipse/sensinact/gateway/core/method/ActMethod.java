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
 * Extended {@link AccessMethod} dedicated to an Actuation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ActMethod extends AbstractAccessMethod<JsonObject, ActResponse> {
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param uri
	 * @param preProcessingExecutor
	 */
	public ActMethod(Mediator mediator, String uri, AccessMethodExecutor preProcessingExecutor) {
		this(mediator, uri, preProcessingExecutor, null);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param uri
	 * @param preProcessingExecutor
	 * @param postProcessingExecutor
	 */
	public ActMethod(Mediator mediator, String uri, AccessMethodExecutor preProcessingExecutor,
			AccessMethodExecutor postProcessingExecutor) {
		super(mediator, uri, AccessMethod.ACT, preProcessingExecutor, postProcessingExecutor, null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AbstractAccessMethod#
	 *      createAccessMethodResponseBuilder(java.lang.Object[])
	 */
	@Override
	protected ActResponseBuilder createAccessMethodResponseBuilder(Object[] parameters) {
		return new ActResponseBuilder(uri, parameters);
	}
}

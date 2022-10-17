/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;

/**
 * An unaccessible {@link ModelElement} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnaccessibleModelElementProxy extends ModelElementProxy {
	/**
	 * Constructor
	 * 
	 * @param resource
	 *            the proxied resource
	 */
	public UnaccessibleModelElementProxy(Mediator mediator, Class<?> proxied, String path) {
		super(mediator, proxied, path);
	}

	/**
	 * Processes an {@link AccessMethod} method invocation on this ModelElementProxy
	 * and returns the {@link AccessMethodResponse} resulting of the invocation
	 * 
	 * @param method
	 *            the {@link AccessMethod.Type} of the {@link AccessMethod} to
	 *            process
	 * @param parameters
	 *            an array of objects containing the values of the arguments passed
	 *            in the method invocation on the proxy instance, or null if
	 *            interface method takes no arguments.
	 * 
	 * @return the resulting {@link AccessMethodResponse}
	 */
	@Override
	public AccessMethodResponse<?> invoke(String type, Object[] parameters) throws Throwable {
		return AccessMethodResponse.error(super.mediator, super.path, AccessMethod.Type.valueOf(type.toUpperCase()),
				SnaErrorfulMessage.FORBIDDEN_ERROR_CODE, "Unaccessible object", null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelElementProxy# getAccessMethod(AccessMethod.Type)
	 */
	@Override
	public AccessMethod<?,?> getAccessMethod(String method) {
		return null;
	}
}

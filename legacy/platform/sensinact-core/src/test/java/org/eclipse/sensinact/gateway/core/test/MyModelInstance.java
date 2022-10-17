/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.test;

import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelElement;
import org.eclipse.sensinact.gateway.core.ModelElementProxy;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.SessionKey;
import org.eclipse.sensinact.gateway.core.message.MessageRouter;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MyModelInstance extends ModelInstance<ModelConfiguration> {
	/**
	 * @param mediator
	 * @param modelConfiguration
	 * @param name
	 * @param profile
	 * @throws InvalidServiceProviderException
	 */
	public MyModelInstance(Mediator mediator, ModelConfiguration modelConfiguration, String name, String profile)
			throws InvalidServiceProviderException {
		super(mediator, modelConfiguration, name, profile);
	}

	public MessageRouter getHandler() {
		return super.messageRouter;
	}

	/**
	 * Returns the {@link AccessLevelOption} for the {@link Session} whose
	 * {@link SessionKey} is passed as parameter, and for the {@link ModelElement}
	 * belonging to this resource model instance whose path is also passed as
	 * parameter
	 * 
	 * @param modelElement
	 *            the targeted resource model element
	 * @param key
	 *            the requirer {@link Session}'s key
	 * 
	 * @return the {@link AccessLevelOption} for the specified session and resource
	 */
	public <I extends ModelInstance<?>, M extends ModelElementProxy, P extends ProcessableData, E extends Nameable, R extends Nameable> AccessLevelOption getAccessLevelOption(
			ModelElement<I, M, P, E, R> modelElement, String publicKey) {
		return AccessLevelOption.ANONYMOUS;
	}

	/**
	 * Returns the set of the specified {@link ModelElement} accessible
	 * {@link AccessMethod.Type}s for the {@link AccessLevelOption} passed as
	 * parameter and
	 * 
	 * @param modelElement
	 *            the {@link ModelElement} for which to retrieve the set of
	 *            accessible {@link AccessMethod.Type}s
	 * @param accessLevelOption
	 *            the requirer {@link AccessLevelOption}
	 * 
	 * @return the set of accessible {@link AccessMethod.Type} of the specified
	 *         {@link ModelElement} for the specified {@link AccessLevelOption}
	 */
	public <I extends ModelInstance<?>, M extends ModelElementProxy, P extends ProcessableData, E extends Nameable, R extends Nameable> List<MethodAccessibility> getAuthorizations(
			ModelElement<I, M, P, E, R> modelElement, AccessLevelOption accessLevelOption) {
		final String path = modelElement.getPath();
		List<MethodAccessibility> methodAccessibilities = this.configuration().getAccessibleMethods(path,
				accessLevelOption);
		return methodAccessibilities;
	}
}

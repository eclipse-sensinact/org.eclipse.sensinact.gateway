/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Authentication service factory
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface UserKeyBuilderFactory<A, C extends Authentication<A>, S extends UserKeyBuilder<A,C>> {
	/**
	 * Returns the type registered into the OSGi host environment's registry as
	 * {@link UserKeyBuilder} service by this factory
	 * 
	 * @return the registered {@link UserKeyBuilder} service type
	 */
	Class<S> getType();

	/**
	 * Creates and registers a new {@link UserKeyBuilder} service into the
	 * OSGi registry
	 * 
	 * @param mediator the {@link Mediator} allowing to interact with the OSGi host
	 * environment
	 * 
	 * @throws SecuredAccessException
	 */
	void newInstance(Mediator mediator) throws SecuredAccessException;
}

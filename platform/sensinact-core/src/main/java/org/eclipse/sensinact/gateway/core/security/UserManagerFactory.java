/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * {@link UserManager} service registerer
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface UserManagerFactory<U extends UserManager> {
	/**
	 * Returns the type registered into the OSGi host environment's registry as
	 * {@link UserManager} service by this factory
	 * 
	 * @return the registered {@link UserManager} service type
	 */
	Class<U> getType();

	/**
	 * Creates and registers a new {@link UserManager} service into the OSGi
	 * registry
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * 
	 * @throws SecuredAccessException
	 */
	void newInstance(Mediator mediator) throws SecuredAccessException;
}

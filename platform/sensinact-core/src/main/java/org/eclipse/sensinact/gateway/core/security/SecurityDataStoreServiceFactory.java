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
 * Factory of {@link SecurityDataStoreService}
 *
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface SecurityDataStoreServiceFactory<S extends SecurityDataStoreService> {
	/**
	 * Returns the type registered into the OSGi host environment's registry as
	 * {@link SecurityDataStoreService} service by this factory
	 * 
	 * @return the registered {@link SecurityDataStoreService} service type
	 */
	Class<S> getType();

	/**
	 * Creates and registers a new {@link SecurityDataStoreService} service into the
	 * OSGi registry
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * 
	 * @throws SecuredAccessException
	 */
	void newInstance(Mediator mediator) throws SecuredAccessException;
}

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
 * Factory providing a {@link SecuredAccess} instance
 *
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface SecuredAccessFactory {
	/**
	 * Creates and returns a {@link SecuredAccess} service
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * 
	 * @throws SecuredAccessException
	 */
	SecuredAccess newInstance(Mediator mediator) throws SecuredAccessException;
}

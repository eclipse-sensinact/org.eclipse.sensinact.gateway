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

import org.osgi.framework.ServiceRegistration;

/**
 * A temporary data structure whose purpose is to make an update on a user when
 * it is validated by the appropriate String token : by simply creating it, or
 * by updating the value of one of its fields
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface UserUpdater {
	
	/**
	 * Validates the update operation held by this UserUpdater and realizes it
	 * 
	 * @param token
	 *       the String token validating this UserUpdater
	 * 
	 * @return the String result object of the update (implementation dependent)
	 * @throws SecuredAccessException 
	 */
	String validate(String token) throws SecuredAccessException;
	
	/**
	 * Defines the {@link ServiceRegistration} of the callback service associated to 
	 * this UserUpdater if any
	 * 
	 * @param registration
	 * 		the {@link ServiceRegistration} of the associated callback service
	 */
	void setRegistration(ServiceRegistration<?> registration);
	
	/**
	 * 
	 * @return
	 */
	String getAccount();
	
	/**
	 * @return
	 */
	String getAccountType();

	/**
	 * @return
	 */
	String getMessage();
	
	/**
	 * @return
	 */
	String getUpdateType();
}

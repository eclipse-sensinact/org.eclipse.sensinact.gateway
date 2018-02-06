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

package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;

public interface NorthboundAccessWrapper 
{
	/**
	 * Returns the {@link NorthboundMediator} of this
	 * NorthboundAccessWrapper, allowing to interact with
	 * the OSGi host environment 
	 * 
	 * @return the {@link NorthboundMediator} of this
	 * NorthboundAccessWrapper
	 */
	NorthboundMediator getMediator();

	/**
	 * @return
	 */
	String getRequestURI();

	/**
	 * Returns the String identifier of the request wrapped
	 * by this NorthboundAccessWrapper if it exists. Otherwise
	 * returns null
	 * 
	 * @param parameters the Set of {@link Parameter}s held 
	 * by the wrapped request
	 * 
	 * @return the wrapped request's String identifier
	 */
	String getRequestID(Parameter[] parameters);

	/**
	 * @return
	 */
	Map<String,List<String>> getQueryMap();

	/**
	 * @return
	 */
	String getContent();

	/**
	 * @return
	 */
	Authentication<?> getAuthentication();

	/**
	 * @param parameters
	 * @return
	 */
	NorthboundRecipient createRecipient(Parameter[] parameters);
}

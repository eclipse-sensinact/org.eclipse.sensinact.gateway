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

package org.eclipse.sensinact.gateway.nthbnd.rest.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;

public interface RequestWrapper 
{
	/**
	 * @return
	 */
	NorthboundMediator getMediator();

	/**
	 * @return
	 */
	String getRequestURI();

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

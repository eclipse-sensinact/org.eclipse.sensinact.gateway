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

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;

/**
 * An MethodAccessibility defines whether an {@link AccessMethod.Type} is
 * accessible for a specific {@link AccessLevelOption}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MethodAccessibility extends Nameable {
	/**
	 * The {@link AccessMethod.Type} of this MethodAccessibility
	 * 
	 * @return this MethodAccessibility's {@link AccessMethod.Type}
	 */
	AccessMethod.Type getMethod();

	/**
	 * The {@link AccessLevelOption} of this MethodAccessibility
	 * 
	 * @return this MethodAccessibility's {@link AccessLevelOption}
	 */
	AccessLevelOption getAccessLevelOption();

	/**
	 * Returns true if the method held by this MethodAccessibility is accessible;
	 * false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if the method is accessible</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean isAccessible();
}

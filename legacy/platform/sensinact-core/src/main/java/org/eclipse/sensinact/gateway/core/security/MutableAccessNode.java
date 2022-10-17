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

import org.eclipse.sensinact.gateway.core.method.AccessMethod;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MutableAccessNode extends AccessNode {
	/**
	 * Defines the Map of available {@link AccessMethod.Type} for all pre-defined
	 * {@link AccessLevelOption}s according to the one passed as parameter
	 * 
	 * @param option
	 *            the {@link AccessProfileOption} holding the {@link AccessProfile}
	 *            for which to build the Map of available {@link AccessMethod.Type}
	 *            for pre-defined {@link AccessLevelOption}s
	 */
	void withAccessProfile(AccessProfileOption option);

	/**
	 * Defines the Map of available {@link AccessMethod.Type} for all pre-defined
	 * {@link AccessLevelOption}s according to the {@link AccessProfile} passed as
	 * parameter
	 * 
	 * @param profile
	 *            the {@link AccessProfile} for which to build the Map of available
	 *            {@link AccessMethod.Type} for pre-defined
	 *            {@link AccessLevelOption}s
	 */
	void withAccessProfile(AccessProfile profile);

	/**
	 * @return
	 */
	MutableAccessNode clone();
}

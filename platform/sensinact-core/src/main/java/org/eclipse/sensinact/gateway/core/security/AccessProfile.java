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

import java.util.Set;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;

/**
 * An AccessProfile maps {@link AccessLevel}s to the set of existing
 * {@link AccessMethod.Type}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessProfile {
	/**
	 * Returns the set of {@link MethodAccess} this AccessProfile gathers
	 * 
	 * @return this AccessProfile's {@link MethodAccess}es
	 */
	Set<MethodAccess> getMethodAccesses();
}

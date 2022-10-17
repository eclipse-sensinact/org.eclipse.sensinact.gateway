/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import java.util.List;

import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 * The sensiNact resource model element definition
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SensiNactResourceModelElement<M extends ModelElementProxy> {
	/**
	 * Creates and returns an {@link ModelElementProxy} of this
	 * {@link SensiNactResourceModelElement}
	 * 
	 * @param methodAccessibilities
	 *            the array of {@link MethodAccessibility}s applying on the
	 *            {@link ModelElementProxy} to be built
	 * 
	 * @return a new {@link SensiNactResourceModelElementProxy}
	 */
	M getProxy(List<MethodAccessibility> methodAccessibilities);

}

/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security;

import java.util.List;

import org.eclipse.sensinact.gateway.core.ModelElement;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;

/**
 * An node of an {@link AccessTree}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessNode {
	/**
	 * Returns the list of {@link MethodAccessibility} available for the
	 * {@link ModelElement} mapped to this AccessNode and the
	 * {@link AccessLevelOption} passed as parameter
	 * 
	 * @param userAccessEntity
	 *            the {@link AccessLevelOption} for which to retrieve the set of
	 *            {@link MethodAccessibility}
	 * 
	 * @return the set of {@link MethodAccessibility} for the mapped
	 *         {@link ModelElement} and the specified {@link AccessLevelOption}
	 */
	List<MethodAccessibility> getAccessibleMethods(AccessLevelOption accessLevelOption);

	/**
	 * Returns the 'minimum' required {@link AccessLevelOption} allowing to invoke
	 * the {@link AccessMethod.Type} passed as parameter on a {@link ModelElement}
	 * mapped to this AccessNode
	 * 
	 * @param method
	 *            the targeted {@link AccessMethod.Type}
	 * 
	 * @return the minimum required {@link AccessLevelOption} to be allowed to
	 *         invoke the specified the {@link AccessMethod.Type}
	 */
	AccessLevelOption getAccessLevelOption(Type method);

	/**
	 * Returns the child AccessNode of this one whose path is passed as parameter,
	 * if it exists
	 * 
	 * @param path
	 *            the path of the child AccessNode
	 * 
	 * @return the child AccessNode with the specified path
	 */
	AccessNode get(String path);

	/**
	 * Returns the {@link AccessProfile} applying on this AccessNode
	 * 
	 * @return this AccessNode's {@link AccessProfile}
	 */
	AccessProfile getProfile();
}

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Basis {@link AccessProfile} implementation
 */
public class AccessProfileImpl implements AccessProfile {
	private HashSet<MethodAccess> methodAccesses;

	/**
	 * Constructor
	 * 
	 * @param methodAccesses
	 *            the Set of {@link MethodAccess} of the {@link AccessProfile} to be
	 *            instantiated
	 */
	public AccessProfileImpl(Set<MethodAccess> methodAccesses) {
		this.methodAccesses = new HashSet<MethodAccess>(methodAccesses);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see AccessProfile#getMethodAccesses()
	 */
	@Override
	public Set<MethodAccess> getMethodAccesses() {
		return Collections.<MethodAccess>unmodifiableSet(this.methodAccesses);
	}

}

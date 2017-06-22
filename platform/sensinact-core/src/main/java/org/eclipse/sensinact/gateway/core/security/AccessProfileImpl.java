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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Basis {@link AccessProfile} implementation
 */
public class AccessProfileImpl implements AccessProfile
{
	private HashSet<MethodAccess> methodAccesses;

	/**
	 * Constructor
	 * 
	 * @param methodAccesses the Set of {@link MethodAccess} of
	 * the {@link AccessProfile} to be instantiated
	 */
	public AccessProfileImpl(Set<MethodAccess> methodAccesses)
	{
		this.methodAccesses = new HashSet<MethodAccess>(methodAccesses);
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see AccessProfile#getMethodAccesses()
	 */
	@Override
	public Set<MethodAccess> getMethodAccesses() 
	{
		return Collections.<MethodAccess>unmodifiableSet(
				this.methodAccesses);
	}

}

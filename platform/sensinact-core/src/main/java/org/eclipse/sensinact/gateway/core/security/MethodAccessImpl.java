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
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MethodAccessImpl implements MethodAccess {
	private final AccessLevel accessLevel;
	private final AccessMethod.Type method;

	/**
	 * Constructor
	 * 
	 * @param accessLevel
	 * @param method
	 */
	public MethodAccessImpl(AccessLevel accessLevel, AccessMethod.Type method) {
		this.accessLevel = accessLevel;
		this.method = method;
	}

	/**
	 * @inheritDoc
	 *
	 * @see MethodAccess#getMethod()
	 */
	@Override
	public AccessMethod.Type getMethod() {
		return this.method;
	}

	/**
	 * @inheritDoc
	 *
	 * @see MethodAccess#getAccessLevel()
	 */
	@Override
	public AccessLevel getAccessLevel() {
		return this.accessLevel;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.method.name();
	}

	public String toString() {
		return new StringBuilder().append(this.method).append(this.accessLevel).toString();
	}

}

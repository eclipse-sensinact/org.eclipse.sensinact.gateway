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

/**
 * Pre-defined set of {@link AccessLevel} policies
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public enum AccessLevelOption {
	UNAUTHORIZED(0), ANONYMOUS(1), AUTHENTICATED(2), ADMIN(3), OWNER(4);

	private final AccessLevel accessLevel;

	/**
	 * Returns the {@link AccessProfile} associated to this access policy
	 * 
	 * @return this access policy's {@link AccessProfile}
	 */
	public AccessLevel getAccessLevel() {
		return this.accessLevel;
	}

	/**
	 * Constructor
	 * 
	 * @param map
	 */
	AccessLevelOption(int level) {
		this.accessLevel = new AccessLevelImpl(level);
	}

	/**
	 * Returns the AccessLevelOption providing the {@link AccessLevel} at the same
	 * level than the one passed as parameter. If no AccessLevelOption is found, the
	 * UNAUTHORIZED one id returned by default
	 * 
	 * @param accessLevel
	 * 
	 * @return
	 */
	public static AccessLevelOption valueOf(AccessLevel accessLevel) {
		// TODO : Define a constant value of the
		// AccessLevelOption to use when the argument
		// is null
		if (accessLevel == null) {
			return AccessLevelOption.ANONYMOUS;
		}
		AccessLevelOption option = null;
		AccessLevelOption[] options = AccessLevelOption.values();
		int index = 0;
		int length = options == null ? 0 : options.length;

		for (; index < length; index++) {
			if (options[index].getAccessLevel().getLevel() == accessLevel.getLevel()) {
				option = options[index];
				break;
			}
		}
		return option == null ? AccessLevelOption.UNAUTHORIZED : option;
	}
}
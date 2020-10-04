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

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessLevelImpl implements AccessLevel {
	private final int level;

	/**
	 * Constructor
	 * 
	 * @param level
	 *            this AccessLevel's int level
	 */
	public AccessLevelImpl(int level) {
		this.level = level;
	}

	/**
	 * @inheritDoc
	 * 
	 * 
	 * @see AccessLevel#getLevel()
	 */
	@Override
	public int getLevel() {
		return this.level;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (AccessLevel.class.isAssignableFrom(object.getClass())) {
			return this.level == ((AccessLevel) object).getLevel();
		}
		return false;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.level;
	}
}

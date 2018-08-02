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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;

/**
 * Basis {@link MethodAccessibility} interface implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MethodAccessibilityImpl implements MethodAccessibility {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	/**
	 * @param method
	 * @param option
	 * @return
	 */
	public static MethodAccessibility unaccessible(Type method, AccessLevelOption option) {
		return new MethodAccessibilityImpl(method, option, false);
	}

	/**
	 * @param option
	 * @return
	 */
	public static List<MethodAccessibility> unaccessible(AccessLevelOption option) {
		List<MethodAccessibility> accessibilities = new ArrayList<MethodAccessibility>();
		AccessMethod.Type[] types = AccessMethod.Type.values();
		int index = 0;
		int length = types == null ? 0 : types.length;

		for (; index < length; index++) {
			accessibilities.add(new MethodAccessibilityImpl(types[index], option, false));
		}
		return accessibilities;
	}

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private final Type method;
	private final AccessLevelOption option;
	private final boolean accessible;

	/**
	 * Constructor
	 * 
	 * @param methodAccess
	 * @param option
	 */
	public MethodAccessibilityImpl(MethodAccess methodAccess, AccessLevelOption option) {
		this(methodAccess.getMethod(), option,
				methodAccess.getAccessLevel().getLevel() <= option.getAccessLevel().getLevel());
	}

	/**
	 * Constructor
	 * 
	 * @param method
	 * @param option
	 * @param accessible
	 */
	public MethodAccessibilityImpl(AccessMethod.Type method, AccessLevelOption option, boolean accessible) {
		if (option == null) {
			throw new NullPointerException("NULL OPTION");
		}
		this.method = method;
		this.option = option;
		this.accessible = accessible;
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

	/**
	 * @inheritDoc
	 *
	 * @see MethodAccessibility#getMethod()
	 */
	@Override
	public Type getMethod() {
		return this.method;
	}

	/**
	 * @inheritDoc
	 *
	 * @see MethodAccessibility#getAccessLevelOption()
	 */
	@Override
	public AccessLevelOption getAccessLevelOption() {
		return this.option;
	}

	/**
	 * @inheritDoc
	 *
	 * @see MethodAccessibility#isAccessible()
	 */
	@Override
	public boolean isAccessible() {
		return this.accessible;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (AccessLevelOption.class == object.getClass()) {
			return object.equals(this.option);
		}
		if (String.class == object.getClass()) {
			return object.equals(this.getName());
		}
		if (AccessMethod.Type.class == object.getClass()) {
			return object.equals(this.method);
		}
		if (MethodAccessibility.class.isAssignableFrom(object.getClass())) {
			return this.equals(((MethodAccessibility) object).getMethod())
					&& this.equals(((MethodAccessibility) object).getAccessLevelOption());
		}
		return false;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String m = method == null ? "NO_METHOD" : method.name();
		String o = option == null ? "NO_OPTION" : option.name();

		return new StringBuilder().append(m).append("[Level:").append(o).append("]").append("[Accessible:")
				.append(accessible).append("]").toString();
	}
}

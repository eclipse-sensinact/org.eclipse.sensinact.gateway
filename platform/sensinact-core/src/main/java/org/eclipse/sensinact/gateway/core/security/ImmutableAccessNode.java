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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.util.tree.ImmutablePathNode;
import org.eclipse.sensinact.gateway.util.tree.PathNodeList;

/**
 * Extended {@link ImmutablePathNode}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public final class ImmutableAccessNode extends ImmutablePathNode<ImmutableAccessNode> implements AccessNode {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private final Map<AccessLevelOption, List<MethodAccessibility>> accesses;
	private final AccessProfile profile;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 */
	public <A extends AccessNodeImpl<A>> ImmutableAccessNode(ImmutableAccessNode parent, String nodeName,
			boolean isPattern, PathNodeList<A> children, AccessProfile profile) {
		super(parent, nodeName, isPattern, children);
		this.accesses = this.withAccessProfile(profile);
		this.profile = profile;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.AccessNode#getProfile()
	 */
	@Override
	public AccessProfile getProfile() {
		return this.profile;
	}

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
	private Map<AccessLevelOption, List<MethodAccessibility>> withAccessProfile(AccessProfile profile) {
		Map<AccessLevelOption, List<MethodAccessibility>> accesses = new EnumMap<AccessLevelOption, List<MethodAccessibility>>(
				AccessLevelOption.class);

		if (profile == null) {
			return Collections.unmodifiableMap(accesses);
		}

		Set<MethodAccess> methodAccesses = profile.getMethodAccesses();
		if (methodAccesses.isEmpty()) {
			return Collections.unmodifiableMap(accesses);
		}
		int[] accessLevels = null;

		Iterator<AccessLevelOption> iterator = Arrays.asList(AccessLevelOption.values()).iterator();

		AccessMethod.Type[] types = AccessMethod.Type.values();

		accessLevels = new int[types.length];
		Iterator<MethodAccess> accessesIterator = methodAccesses.iterator();
		while (accessesIterator.hasNext()) {
			MethodAccess methodAccess = accessesIterator.next();
			int level = methodAccess.getAccessLevel().getLevel();
			int ind = methodAccess.getMethod().ordinal();
			accessLevels[ind] = level;
		}
		int index = -1;
		int length = types == null ? 0 : types.length;

		while (iterator.hasNext()) {
			index = 0;
			AccessLevelOption optionLevel = iterator.next();

			for (; index < length; index++) {
				boolean accessibility = accessLevels[index] <= optionLevel.getAccessLevel().getLevel();

				List<MethodAccessibility> accessibilities = accesses.get(optionLevel);

				if (accessibilities == null) {
					accessibilities = new ArrayList<MethodAccessibility>();
					accesses.put(optionLevel, accessibilities);
				}
				MethodAccessibilityImpl methodAccessibilities = new MethodAccessibilityImpl(types[index], optionLevel,
						accessibility);
				accessibilities.add(methodAccessibilities);
			}
		}
		return Collections.unmodifiableMap(accesses);
	}

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
	@Override
	public List<MethodAccessibility> getAccessibleMethods(AccessLevelOption accessLevelOption) {
		List<MethodAccessibility> accesses = new ArrayList<MethodAccessibility>();
		AccessMethod.Type[] types = AccessMethod.Type.values();

		int index = 0;
		int length = types.length;

		for (; index < length; index++) {
			accesses.add(new MethodAccessibilityImpl(types[index], accessLevelOption,
					this.isAccessibleMethod(types[index], accessLevelOption)));
		}
		return accesses;
	}

	/**
	 * Returns true if the {@link AccessMethod.Type} passed as parameter is
	 * available at the {@link AccessLevel} passed as parameter; otherwise returns
	 * false
	 * 
	 * @param accessMethod
	 *            the {@link AccessMethod.Type} to define the availability of for
	 *            the specified {@link AccessLevel}
	 * @param accessLevel
	 *            the {@link AccessLevel} for which to define the specified
	 *            {@link AccessMethod.Type} availability
	 * @return
	 *         <ul>
	 *         <li>true if the specified {@link AccessMethod.Type} is accessible for
	 *         the specified {@link AccessLevel}</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	protected boolean isAccessibleMethod(AccessMethod.Type accessMethod, AccessLevelOption optionLevel) {
		int index = -1;
		boolean accessible = false;

		List<MethodAccessibility> methodAccesses;

		if (this.accesses == null || (methodAccesses = this.accesses.get(optionLevel)) == null
				|| (index = methodAccesses.indexOf(new Name<MethodAccessibility>(accessMethod.name()))) == -1) {
			accessible = super.parent == null ? false : super.parent.isAccessibleMethod(accessMethod, optionLevel);
		} else {
			MethodAccessibility access = methodAccesses.get(index);
			accessible = access == null ? false : access.isAccessible();
		}
		return accessible;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.AccessNode#
	 *      getAccessLevelOption(org.eclipse.sensinact.gateway.core.method.AccessMethod.Type)
	 */
	@Override
	public AccessLevelOption getAccessLevelOption(Type method) {
		AccessLevelOption[] accessLevelOptions = AccessLevelOption.values();
		int index = 0;
		int length = accessLevelOptions == null ? 0 : accessLevelOptions.length;
		for (; index < length; index++) {
			if (this.isAccessibleMethod(method, accessLevelOptions[index])) {
				return accessLevelOptions[index];
			}
		}
		return AccessLevelOption.OWNER;
	}
}

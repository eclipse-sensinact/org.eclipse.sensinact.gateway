/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 * 
 */
package org.eclipse.sensinact.gateway.core.security;

import org.eclipse.sensinact.gateway.util.tree.ImmutablePathTree;

/**
 * Extended {@link ImmutablePathTree} holding {@link ImmutableAccessNode}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ImmutableAccessTree extends ImmutablePathTree<ImmutableAccessNode>
		implements AccessTree<ImmutableAccessNode> {
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

	/**
	 * Constructor
	 * 
	 * @param root
	 *            the root ImmutableAccessNode of the Immutable AccessTree to be
	 *            instantiated
	 */
	public ImmutableAccessTree(ImmutableAccessNode root) {
		super(root);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.AccessTree#isMutable()
	 */
	@Override
	public boolean isMutable() {
		return false;
	}
}

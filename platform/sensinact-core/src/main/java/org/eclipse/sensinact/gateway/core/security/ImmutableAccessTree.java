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
/**
 * 
 */
package org.eclipse.sensinact.gateway.core.security;

import org.eclipse.sensinact.gateway.util.tree.ImmutablePathTree;

/**
 * Extended {@link ImmutablePathTree} holding {@link ImmutableAccessNode}s
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
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

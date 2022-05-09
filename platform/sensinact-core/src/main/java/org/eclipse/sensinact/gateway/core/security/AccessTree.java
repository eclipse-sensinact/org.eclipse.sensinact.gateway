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

import org.eclipse.sensinact.gateway.util.tree.PathTree;

/**
 * Extended {@link PathTree} gathering access rights to a sensiNact gateway's
 * data model instance using an hierarchical data structure
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessTree<A extends AccessNode> {
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
	 * Returns the root {@link AccessNode} of this AccessTree
	 * 
	 * @return this AccessTree's root node
	 */
	A getRoot();

	/**
	 * Returns true if this AccessTree is mutable, meaning that it is possible to
	 * add/remove {@link AccessNode} to/from it
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if this AccessTree is mutable</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean isMutable();
}

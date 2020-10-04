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
/**
 * 
 */
package org.eclipse.sensinact.gateway.core.security;

/**
 * Extended {@link AccessTree} gathering access rights to a sensiNact gateway's
 * data model instances using an hierarchical data structure
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MutableAccessTree<A extends MutableAccessNode> extends AccessTree<A>, Cloneable {
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
	 * Adds an {@link AccessNode} to this MutableAccessTree at the path passed as
	 * parameter
	 * 
	 * @param uri
	 *            the String path of the {@link AccessNode} to be added
	 * @return the newly created {@link AccessNode}
	 */
	A add(String uri);

	/**
	 * Adds an {@link AccessNode} to this MutableAccessTree at the path passed as
	 * parameter
	 * 
	 * @param uri
	 *            the String path of the {@link AccessNode} to be added
	 * @param isPattern
	 *            defines whether the name of the {@link AccessNode} to be added is
	 *            a regular expression
	 * @return the newly created {@link AccessNode}
	 */
	A add(String uri, boolean isPattern);

	/**
	 * Adds an {@link AccessNode} to this MutableAccessTree at the path passed as
	 * parameter
	 * 
	 * @param name
	 *            the String path of the {@link AccessNode} to be added
	 * @param uriElement
	 * 
	 * @return the newly created {@link AccessNode}
	 */
	A add(String name, String uriElement);

	/**
	 * Adds an {@link AccessNode} to this MutableAccessTree at the path passed as
	 * parameter
	 * 
	 * @param name
	 *            the String path of the {@link AccessNode} to be added
	 * @param uriElement
	 * @param isPattern
	 *            defines whether the name of the {@link AccessNode} to be added is
	 *            a regular expression
	 * 
	 * @return the newly created {@link AccessNode}
	 */
	A add(String name, String uriElement, boolean isPattern);

	/**
	 * Removes the {@link AccessNode} from this MutableAccessTree at the path passed
	 * as parameter
	 * 
	 * @param uri
	 *            the String path of the {@link AccessNode} to be removed
	 * 
	 * @return the removed {@link AccessNode}
	 */
	A delete(String uri);

	/**
	 * 
	 * @param option
	 *            the {@link AccessProfileOption} wrapping the {@link AccessProfile}
	 *            applying to the root node of this {@link AccessTree}
	 * 
	 * @return this AccessTree instance
	 */
	MutableAccessTree<A> withAccessProfile(AccessProfile profile);

	/**
	 * @return
	 */
	MutableAccessTree<A> clone();

	ImmutableAccessTree immutable();

}

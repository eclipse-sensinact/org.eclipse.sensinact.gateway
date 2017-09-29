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
package org.eclipse.sensinact.gateway.util.tree;

/**
 * Factory of {@link PathNode}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PathNodeFactory<P extends PathNode<P>>
{
	/**
	 * Returns a {@link PathNode} whose name is 
	 * passed as parameter 
	 * 
	 * @param nodeName the name of the {@link PathNode}
	 * to be created
	 * 
	 * @return the newly created {@link PathNode}
	 */
	P createPathNode(String nodeName);

	/**
	 * Returns a {@link PathNode} whose pattern is 
	 * passed as parameter 
	 * 
	 * @param nodeName the pattern of the {@link PathNode}
	 * to be created
	 * 
	 * @return the newly created {@link PathNode}
	 */
	P createPatternNode(String nodeName);

	/**
	 * Returns a new root {@link PathNode} 
	 * 
	 * @return the newly created root {@link PathNode}
	 */
	P createRootNode();
}
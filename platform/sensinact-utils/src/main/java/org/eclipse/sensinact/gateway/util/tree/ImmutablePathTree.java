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
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ImmutablePathTree<P extends ImmutablePathNode<P>>
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//
		
	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//
		
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	protected final P root;
	
	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public ImmutablePathTree(P root)
	{
		this.root = root;
	}

	/**
	 * Returns the root {@link PathNode} of this
	 * PathTree
	 *
	 * @return this PathTree root {@link PathNode}
	 */
	public P getRoot()
	{
		return this.root;
	}
	
	/**
	 * @param uri
	 * @return
	 */
	public P get(String uri)
	{
		return  this.root.get(uri);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return this.root.toString();
	}
}

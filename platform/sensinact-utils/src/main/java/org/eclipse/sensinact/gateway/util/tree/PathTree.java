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

import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PathTree<N extends PathNode>
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

	protected final N root;
	protected final PathNodeFactory<N> factory;
	
	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public PathTree(PathNodeFactory<N> factory)
	{
		this.factory = factory;
		this.root = this.factory.createRootNode();
	}

	/**
	 * @param uri
	 */
	public N add(String uri)
	{
		String[] uriElements = UriUtils.getUriElements(uri);
		StringBuilder builder = new StringBuilder();
		
		int index = 0;
		int length = uriElements==null?0:uriElements.length;
		
		N node = null;
		
		for(;index < length; index++)
		{
			builder.append(UriUtils.PATH_SEPARATOR);
			node = add(builder.toString(),uriElements[index]);
			builder.append(uriElements[index]);	
		}
		return node;
	}

	/**
	 * @param uri
	 * @param uriElement
	 */
	public N add(String uri, String uriElement)
	{
		return this.add(uri, uriElement, false);
	}

	/**
	 * @param uri
	 * @param uriElement
	 * @param isPattern
	 */
	public N add(String uri, String uriElement, boolean isPattern)
	{
		N current = this.get(uri);
		if(current != null)
		{
			current = (N) current.add(isPattern
				?factory.createPatternNode(uriElement)
				:factory.createPathNode(uriElement));
		}
		return current;
	}

	/**
	 * Returns the root {@link PathNode} of this
	 * PathTree
	 *
	 * @return this PathTree root {@link PathNode}
	 */
	public N getRoot()
	{
		return this.root;
	}
	
	/**
	 * @param uri
	 * @return
	 */
	public N get(String uri)
	{
		return (N) this.root.get(uri);
	}
	
	/**
	 * @param uri
	 * @return
	 */
	public N delete(String uri)
	{
		String parentUri = UriUtils.getParentUri(uri);
		PathNode parent = this.root.get(parentUri);
		if(parent != null)
		{
			return (N) parent.remove(UriUtils.getLeaf(uri));
		}
		return null;
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

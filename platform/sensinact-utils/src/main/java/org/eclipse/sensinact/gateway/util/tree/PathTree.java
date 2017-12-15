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
public class PathTree<P extends PathNode<P>>
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
	protected final PathNodeFactory<P> factory;
	
	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public PathTree(PathNodeFactory<P> factory)
	{
		this.factory = factory;
		this.root = this.factory.createRootNode();
	}

	/**
	 * @param uri
	 */
	public P add(String uri)
	{
		return this.add(uri, false);
	}

	/**
	 * @param uri
	 * @param isPattern
	 * @return
	 */
	public P add(String uri, boolean isPattern)
	{
		String[] uriElements = UriUtils.getUriElements(uri);
		StringBuilder builder = new StringBuilder();
		
		int index = 0;
		int length = uriElements==null?0:uriElements.length;
		
		P node = null;
		
		for(;index < length; index++)
		{
			builder.append(UriUtils.PATH_SEPARATOR);
			if(length-index==1)
			{
				node = add(builder.toString(), uriElements[index], isPattern);
			} else
			{
				node = add(builder.toString(), uriElements[index], false);
			}
			builder.append(uriElements[index]);	
		}
		return node;
	}

	
	/**
	 * @param uri
	 * @param uriElement
	 */
	public P add(String uri, String uriElement)
	{
		return this.add(uri, uriElement, false);
	}

	/**
	 * @param uri
	 * @param uriElement
	 * @param isPattern
	 */
	public P add(String uri, String uriElement, boolean isPattern)
	{
		P current = this.get(uri);
		if(current != null)
		{
			current = current.add(isPattern
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
		return this.root.get(uri);
	}
	
	/**
	 * @param uri
	 * @return
	 */
	public P delete(String uri)
	{
		String parentUri = UriUtils.getParentUri(uri);
		P parent = this.root.get(parentUri);
		if(parent != null)
		{
			return parent.remove(UriUtils.getLeaf(uri));
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
	
	/**
	 * @param ic
	 * @return
	 */
	public <N extends ImmutablePathNode<N>, T extends ImmutablePathTree<N>> 
	T immutable(Class<T> tc, Class<N> ic)
	{
		try
		{
			return tc.getConstructor(new Class<?>[]{ic}
			).newInstance(this.root.<N>immutable(ic, null));
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}

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

import java.util.Iterator;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PathNode
{
	protected PathNode parent;
	protected final String nodeName;
	protected final PathNodeList children;
	protected final Pattern pattern;
	
	protected final boolean isRoot;
	protected final boolean isPattern;
	
	/**
	 * Constructor
	 * 
	 * @param nodeName the name of the PathNode
	 * to be instantiated
	 */
	public PathNode()
	{
		this(UriUtils.PATH_SEPARATOR);
	}

	/**
	 * Constructor
	 * 
	 * @param nodeName the name of the PathNode
	 * to be instantiated
	 */
	public PathNode(String nodeName)
	{
		this(nodeName, false);
	}
	
	/**
	 * Constructor
	 * 
	 * @param nodeName the name of the PathNode
	 * to be instantiated
	 */
	public PathNode(String nodeName, boolean isPattern)
	{
		this.nodeName = nodeName;
		this.children = new PathNodeList();
		this.isRoot = (nodeName.intern() == UriUtils.PATH_SEPARATOR);
		this.isPattern = isPattern;
		this.pattern = isPattern?Pattern.compile(nodeName):null;
	}

	/**
	 * @param path
	 * @return
	 */
	public PathNode get(String path)
	{
		return this.get(UriUtils.getUriElements(path), 0);
	}

	/**
	 * @param path
	 * @param index
	 * @return
	 */
	public PathNode get(String[] path, int index)
	{
		PathNode node = null;
		
		if((isRoot && index!=0) || 
			(!isRoot && (path.length - index < 1 
					|| !this.equals(path[index]))))
		{
			return node;
		}
		int inc = isRoot?0:1;
		node = this;	
		PathNode child = null;
		
		if(path.length - index > inc && (child = 
			this.children.get(path[index+inc]))!=null)	
		{
			node = child.get(path, index+inc);
		}
		return node;
	}

	/**
	 * @param node
	 * @return
	 */
	public PathNode add(PathNode node)
	{
		if(node == null)
		{
			return null;
		}
		PathNode child = this.children.get(node.nodeName);
		if(child != null)
		{
			return child;
		}
		node.parent = this;
		return this.children.add(node);
	}

	/**
	 * @param nodeName
	 * @return
	 */
	public PathNode remove(String nodeName)
	{
		return this.children.remove(nodeName);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return nodeName.hashCode();
	}

	/**
	 * Returns the name of this PathNode
	 * 
	 * @return this PathNode's name
	 */
	public String getName()
	{
		return this.nodeName;
	}

	/**
	 * Returns the path of this PathNode
	 * 
	 * @return this PathNode's path
	 */
	public String getPath()
	{
		if(this.parent == null)
		{
			return "";
		}
		return UriUtils.getUri(
			new String[]{this.parent.getPath(),
					this.nodeName});
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object)
	{
		String objectName = null;
		
		if(PathNode.class.isAssignableFrom(object.getClass()))
		{
			objectName = ((PathNode)object).nodeName;
			
		} else if(String.class == object.getClass())
		{
			objectName = (String) object;
		}
		if(objectName==null)
		{
			return false;
		}
		return !isPattern
			?this.nodeName.equals(objectName)
				:pattern.matcher(objectName
						).matches();
	}

	/**
     * Returns the size of this node, meaning the
     * number of its children
     *
     * @return the number of this node's children
     */
	public int size()
    {
        return this.children.size();
    }
	
	/**
	 * @inheritDoc
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(this.nodeName);
		builder.append("[");
		Iterator<PathNode> iterator = children.iterator();
		while(iterator.hasNext())
		{
			PathNode node = iterator.next();
			builder.append(node.toString());
		}
		builder.append("]");
		return builder.toString();
	}
}
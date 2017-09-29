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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A list of {PathNode}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ImmutablePathNodeList<P extends ImmutablePathNode<P>> implements Iterable<P>
{
	private final Object lock = new Object();
	
	private final List<ImmutablePathNodeBucket<P>> buckets;
	
	/**
	 * Constructor
	 * 
	 * @param buckets
	 */
	protected ImmutablePathNodeList(List<ImmutablePathNodeBucket<P>> buckets)
	{
		this.buckets = Collections.unmodifiableList(buckets);
	}
	
	/**
	 * @param nodeName
	 * 
	 * @return
	 */
	public P get(String nodeName)
	{
		if (nodeName == null || this.buckets.isEmpty())
		{
			return null;
		}
		int hash = nodeName.hashCode();
		hash ^= (hash >>> 20) ^ (hash >>> 12);
		hash ^= (hash >>> 7) ^ (hash >>> 4);
		
		synchronized(lock)
		{
			//search for exact match
			for (ImmutablePathNodeBucket<P> b = 
					buckets.get((hash & (buckets.size() - 2)) + 1);
						b != null; b = b.next)
			{
			    if (b.node.nodeName.equals(nodeName))
			    {
			          return b.node;
			    }
			}
			//search for pattern match
			for (ImmutablePathNodeBucket<P> b = 
					buckets.get(0);b != null; b = b.next)
			{
			    if (b.node.equals(nodeName))
			    {
			          return b.node;
			    }
			}
		}
		return null;
	}
    
    /**
     * Returns this ImmutablePathNodeList's size
     * 
     * @return the size of this ImmutablePathNodeList
     */
    public int size()
    {
    	return this.buckets.size();
    }
    
    /**
     * @inheritDoc
     *
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<P> iterator()
    {
    	return new Iterator<P>()
    	{
    		int position = -1;
    		
    		ImmutablePathNodeBucket<P> bucket = null;
    		P node = null;
    		    		
			/**
			 * @inheritDoc
			 * 
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() 
			{
				if(position == -1)
				{
					next();
				}
				return node != null;
			}

			/**
			 * @inheritDoc
			 * 
			 * @see java.util.Iterator#next()
			 */
			@Override
			public P next() 
			{
				P current = node;
				if(bucket != null)
				{						
					bucket = bucket.next;
				}
				if(bucket == null)
				{
					while(++position < buckets.size() 
					&& (bucket = buckets.get(position))==null);
				}
				node = bucket==null?null:bucket.node;				
				return current;
			}

			/**
			 * @inheritDoc
			 * 
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() 
			{
				//unimplemented
			}
    	};
    }
}
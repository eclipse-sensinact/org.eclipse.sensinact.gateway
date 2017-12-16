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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A list of {PathNode}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PathNodeList<P extends PathNode<P>> implements Iterable<P>
{
	private final Object lock = new Object();
	
	private PathNodeBucket<?>[] table;
	private int threshold = 2;
	private int size = 0;
	private int length = 5;
		
	/**
	 * Constructor
	 */
	protected PathNodeList()
	{
		clear();
	}

	/**
	 * 
	 */
	public void clear()
	{
		this.length = 5;
		this.size = 0;
		this.table = new PathNodeBucket<?>[length];
	}
	
//	/**
//	 */
//	public P get(String nodeName)
//	{
//		if (nodeName == null)
//		{
//			return null;
//		}
//		int hash = nodeName.hashCode();
//		hash ^= (hash >>> 20) ^ (hash >>> 12);
//		hash ^= (hash >>> 7) ^ (hash >>> 4);
//		
//		synchronized(lock)
//		{
//			//search for exact match
//			for (PathNodeBucket<P> b = (PathNodeBucket<P>)
//				table[(hash & (length - 2)) + 1];
//				    b != null; b = b.next)
//			{
//			    if (b.node.nodeName.equals(nodeName))
//			    {
//			          return b.node;
//			    }
//			}
//			//search for pattern match
//			for (PathNodeBucket<P> b = (PathNodeBucket<P>)table[0];
//					b != null; b = b.next)
//			{
//			    if (b.node.equals(nodeName))
//			    {
//			          return b.node;
//			    }
//			}
//		}
//		return null;
//	}
	
	/**
	 * @param nodeName
	 * @return
	 */
	public P getStrictNode(String nodeName)
	{
		if (nodeName == null)
		{
			return null;
		}
		int hash = nodeName.hashCode();
		hash ^= (hash >>> 20) ^ (hash >>> 12);
		hash ^= (hash >>> 7) ^ (hash >>> 4);
		
		synchronized(lock)
		{
			//search for exact match
			for (PathNodeBucket<P> b = (PathNodeBucket<P>)
				table[(hash & (length - 2)) + 1];
				    b != null; b = b.next)
			{
			    if (b.node.nodeName.equals(nodeName))
			    {
			          return b.node;
			    }
			}

			//search for strict match into pattern nodes
			for (PathNodeBucket<P> b = (PathNodeBucket<P>)table[0];
					b != null; b = b.next)
			{
			    if (b.node.nodeName.equals(nodeName))
			    {
			         return b.node;
			    }
			}
		}
		return null;
	}
	
	/**
	 * @param nodeName
	 * @return
	 */
	public List<P> getPatternNodes(String nodeName)
	{
		if (nodeName == null)
		{
			return Collections.<P>emptyList();
		}
		List<P> list = new ArrayList<P>();
		synchronized(lock)
		{
			//search for pattern match
			for (PathNodeBucket<P> b = (PathNodeBucket<P>)table[0];
					b != null; b = b.next)
			{
			    if (b.node.equals(nodeName))
			    {
			          list.add(b.node);
			    }
			}
		}
		return list;
	}

	/**
     * Maps the specified key to the specified value.
     *
     * @param node the PathNode to add.
     * 
     * @return the added PathNode or null if an error 
     * occurred.
     */
     public P add(P node)
     {	
    	if(node == null)
    	{
    		return null;
    	}
		synchronized(lock)
		{
	        if (++size > threshold)
	        {
	        	PathNodeBucket<?>[] oldTable = table;
		        int oldCapacity = length;
		        
		        length = oldCapacity * 2;	        
		        table = new PathNodeBucket<?>[length];
		        threshold = (length >> 1) + (length >> 2);
		        
		        table[0] = oldTable[0];
		        
		        for (int j = 1; j < oldCapacity; j++) 
		        {
		        	PathNodeBucket<P> b = (PathNodeBucket<P>)oldTable[j];	
		        	
		            while(b != null)
		            {
		            	PathNodeBucket<P> p = b;
		            	b = b.next;
		            	p.next = null;
		            	add(p);	            	
		            }
		        }
	        }
	    	add(new PathNodeBucket<P>(node));
		}
		return node;
    }
     
    /**
     * Adds the PathNodeBucket passed as parameter
     * 
     * @param b the PathNodeBucket to be added
     */
    private void add(PathNodeBucket<P> b)
    {
    	int index = 0;
    	if(!b.node.isPattern)
    	{
	    	int hash = b.hash;	        
			hash ^= (hash >>> 20) ^ (hash >>> 12);
			hash ^= (hash >>> 7) ^ (hash >>> 4);				  
		    index = (hash & (length - 2)) + 1;
    	}
	    PathNodeBucket<P> bu = null;
	    if((bu = (PathNodeBucket<P>) table[index])== null)
	    {
	    	table[index] = b;
	    		
	    } else
	    {
	    	for (;bu.next != null; bu = bu.next);
	    	bu.next = b;
	    }
    }
    
    /**
     * Removes the PathNode whose name is passed as 
     * parameter.
     *
     * @param nodeName the name of the PathNode to be
     * removed
     * 
     * @return the remove PathNode
     */
    public P remove(String nodeName) 
    {
        if (nodeName == null) 
        {
            return null;
        }
        int hash = nodeName.hashCode();
	    hash ^= (hash >>> 20) ^ (hash >>> 12);
	    hash ^= (hash >>> 7) ^ (hash >>> 4);
		
	    synchronized(lock)
	    {
	         int index = (hash & (table.length - 2)) + 1;
	         //search for name
	         for (PathNodeBucket<P> b = (PathNodeBucket<P>)table[index], 
	        		 prev = null; b != null; prev = b, b = b.next) 
	         {
	             if (nodeName.equals(b.node.nodeName))
	             {
	                 if (prev == null) 
	                 {
	                     table[index] = b.next;
	                     
	                 } else
	                 {
	                     prev.next = b.next;
	                 }
	                 size--;
	                 return b.node;
	             }
	         }
	         //search for patterns
	         for (PathNodeBucket<P> b = (PathNodeBucket<P> )table[0], 
	        		 prev = null; b != null; prev = b, b = b.next) 
	         {
	             if (nodeName.equals(b.node.nodeName))
	             {
	                 if (prev == null) 
	                 {
	                     table[index] = b.next;
	                     
	                 } else
	                 {
	                     prev.next = b.next;
	                 }
	                 size--;
	                 return b.node;
	             }
	         }
		 }
         return null;
    }
    
    /**
     * Returns this PathNodeList's size
     * 
     * @return the size of this PathNodeList
     */
    public int size()
    {
    	return size;
    }
     
	/**
	 * @param parent
	 * @return
	 */
	public <N extends ImmutablePathNode<N>> 
	ImmutablePathNodeList<N> immutable(final N parent)
	{
		return new ImmutablePathNodeList<N>(
		new ArrayList<ImmutablePathNodeBucket<N>>()
		{
			private static final long serialVersionUID = 1L;

			public List<ImmutablePathNodeBucket<N>> addAll()
			{
				int index = 0;
				int length = PathNodeList.this.table.length;
				for(;index < length; index++)				
				{
					if(PathNodeList.this.table[index]==null)
					{
						super.add(null);
						continue;
					}
					super.add(
						index, PathNodeList.this.table[index].<N>immutable(
								(Class<N>)parent.getClass(), parent));
				}
				return this;
			
			}
		}.addAll());
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
    		
    		PathNodeBucket<P> bucket = null;
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
					while(++position < length 
					&& (bucket = (PathNodeBucket<P>)table[position]
							)==null);
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
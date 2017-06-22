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

/**
 * A list of {PathNode}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PathNodeList implements Iterable<PathNode>
{
	private final Object lock = new Object();
	
	private PathNodeBucket[] table;
	private int threshold = 2;
	private int size = 0;
	private int length = 5;
		
	/**
	 * Constructor
	 */
	protected PathNodeList()
	{
		table = new PathNodeBucket[length];
	}
	
	/**
	 * @param nodeName
	 * 
	 * @return
	 */
	PathNode get(String nodeName)
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
			for (PathNodeBucket b = table[(hash & (length - 2)) + 1];
				b != null; b = b.next)
			{
			    if (b.node.nodeName.equals(nodeName))
			    {
			          return b.node;
			    }
			}
			//search for pattern match
			for (PathNodeBucket b = table[0];b != null; b = b.next)
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
     * Maps the specified key to the specified value.
     *
     * @param node the PathNode to add.
     * 
     * @return the added PathNode or null if an error 
     * occurred.
     */
     public PathNode add(PathNode node)
     {	
    	if(node == null)
    	{
    		return null;
    	}
		synchronized(lock)
		{
	        if (++size > threshold)
	        {
	        	PathNodeBucket[] oldTable = table;
		        int oldCapacity = length;
		        
		        length = oldCapacity * 2;	        
		        table = new PathNodeBucket[length];
		        threshold = (length >> 1) + (length >> 2);
		        
		        table[0] = oldTable[0];
		        
		        for (int j = 1; j < oldCapacity; j++) 
		        {
		        	PathNodeBucket b = oldTable[j];	
		        	
		            while(b != null)
		            {
		            	PathNodeBucket p = b;
		            	b = b.next;
		            	p.next = null;
		            	add(p);	            	
		            }
		        }
	        }
	    	add(new PathNodeBucket(node));
		}
		return node;
    }
     
    /**
     * Adds the PathNodeBucket passed as parameter
     * 
     * @param b the PathNodeBucket to be added
     */
    private void add(PathNodeBucket b)
    {
    	int index = 0;
    	if(!b.node.isPattern)
    	{
	    	int hash = b.hash;	        
			hash ^= (hash >>> 20) ^ (hash >>> 12);
			hash ^= (hash >>> 7) ^ (hash >>> 4);				  
		    index = (hash & (length - 2)) + 1;
    	}
	    PathNodeBucket bu = null;
	    if((bu = table[index])== null)
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
    public PathNode remove(String nodeName) 
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
	         for (PathNodeBucket b = table[index], prev = null;
	                 b != null; prev = b, b = b.next) 
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
	         for (PathNodeBucket b = table[0], prev = null;
	                 b != null; prev = b, b = b.next) 
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
     * @inheritDoc
     *
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<PathNode> iterator()
    {
    	return new Iterator<PathNode>()
    	{
    		int position = -1;
    		
    		PathNodeBucket bucket = null;
    		PathNode node = null;
    		    		
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
			public PathNode next() 
			{
				PathNode current = node;
				if(bucket != null)
				{						
					bucket = bucket.next;
				}
				if(bucket == null)
				{
					while(++position < length 
					&& (bucket = table[position])==null);
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
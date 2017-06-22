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

package org.eclipse.sensinact.gateway.common.primitive;

/**
 * String wrapper for equality evaluation purpose of 
 * {@link Nameable} objects
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Name<N extends Nameable>
{
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param name
	 * 		the wrapped string name 
	 */
	public Name(String name)
	{
		this.name = name; 
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object)
	{
	    boolean equals = false;
		if(this.name == null)
		{
			return equals;
		}
		try
		{
		    @SuppressWarnings("unchecked")
            String name = ((N)object).getName();
			equals = this.name.equals(name);	
			
		} catch(ClassCastException e)
		{
    		if(object.getClass()==String.class)
    		{
    			equals = this.name.equals(
    			        ((String)object));
    		}
		}
		return equals;
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return this.name.hashCode();
	}
}

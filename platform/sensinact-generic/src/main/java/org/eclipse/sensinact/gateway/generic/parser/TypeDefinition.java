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
package org.eclipse.sensinact.gateway.generic.parser;

import org.xml.sax.Attributes;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.CastUtils;

/**
 * A {@link TypeDefinition} defines a simple xml container element
 * potentially holding a target attribute 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "type", field = "type")
public class TypeDefinition extends XmlDefinition
{
    /**
     * the Type specified by the associated
     * xml tag element 
     */
    protected Class<?> type;
    
    /**
     * Constructor
     * 
     * @param mediator
     *      the associated Mediator
     * @param atts
     *      the set of attributes data structure for the 
     *      xml type element
     */
    public TypeDefinition (Mediator mediator, Attributes atts)
    {
        super(mediator, atts);
    }
    
    /**
     * Sets the canonical name of the Type specified 
     * by the associated xml tag element 
     * 
     * @param type
     *      the canonical name of the Type specified 
     *      by the associated xml tag element 
     */
    public void setType(String type)
    {
    	this.type = null;
    	try
    	{
    		this.type = CastUtils.loadClass(
    			super.mediator.getClassLoader(), type);
    		
    	} catch(ClassNotFoundException e)
    	{
            super.mediator.error(new StringBuilder().append(
            		"Invalid type : ").append(type).toString());
        }   
    }  

	/**
	 * Defines this TypeDefinition's type
	 * 
	 * @param type
	 * 		this TypeDefinition's type
	 */
	public void setType(Class<?> type)
	{
		this.type = type;
	}
	
    /**
     * Returns the Type specified by the associated
     * xml tag element 
     * 
     * @return
     *      the Type specified by the associated
     *      xml tag element 
     */
    public Class<?> getType()
    {
        return this.type;
    }

}

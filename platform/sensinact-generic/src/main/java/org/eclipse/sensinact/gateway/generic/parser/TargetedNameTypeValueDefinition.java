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


/**
 * Extended abstract {@link TargetedDefinition} for xml elements gathering a name
 * attribute, as well as types and values elements
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "name", field = "name"),
	@XmlAttribute(attribute = "type", field = "type")})
public abstract class TargetedNameTypeValueDefinition extends TargetedDefinition
{
    /**
     * the {@link TypeDefinition} of this NameTypeValueDefinition
     */
    protected TypeDefinition typeDefinition;

    /**
     * the {@link ValueDefinition} of this NameTypeValueDefinition
     */
    protected ValueDefinition valueDefinition;
    
    protected String name;

    /**
     * Constructor
     * 
     * @param mediator
     *      the associated Mediator
     * @param atts
     *      the set of attributes data structure for the 
     *      xml element
     */
    TargetedNameTypeValueDefinition(Mediator mediator, Attributes atts)
    {
        super(mediator, atts);
    }

    /**
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * @param target
     * @return
     */
    public TypeDefinition getTypeDefinition()
    {
        return this.typeDefinition;
    }    

    /**
     * @param target
     * @return
     */
    public ValueDefinition getValueDefinition()
    {
        return this.valueDefinition;
    }
    
    /**
     * @param valueDefinition
     */
    protected void setValueDefinition(ValueDefinition valueDefinition)
    {
        this.valueDefinition = valueDefinition;
    }

	/**
	 *
	 */
	protected final TypeValuePair getTypeValuePair(String service)
	{
		if(!super.isTargeted(service))
		{
			return null;
		}
        Class<?> type = typeDefinition==null
        		?null:typeDefinition.getType();
        
        return new TypeValuePair(type, valueDefinition!=null
        		?valueDefinition.getValue():null);
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
    	this.typeDefinition = new TypeDefinition(mediator,null);
    	this.typeDefinition.setType(type);
    }  

	/**
	 * Defines this TypeDefinition's type
	 * 
	 * @param type
	 * 		this TypeDefinition's type
	 */
	public void setType(Class<?> type)
	{
    	this.typeDefinition = new TypeDefinition(mediator,null);
    	this.typeDefinition.setType(type);
	}
	
}

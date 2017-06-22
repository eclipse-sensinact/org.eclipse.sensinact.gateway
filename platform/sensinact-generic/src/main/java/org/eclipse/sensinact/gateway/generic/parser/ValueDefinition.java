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
 * A {@link ValueDefinition} defines a simple xml container element
 * potentially holding a target attribute 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "value", field = "value")
public class ValueDefinition extends XmlDefinition
{    
    /**
     * the value object defined in the associated
     * xml element
     */
    protected Object value;
    
    /**
     * the TypeDefinition wrapping the Type of
     * this ValueDefinition's value object
     */
    protected TypeDefinition typeDefinition;
    
    /**
     * Constructor
     * 
     * @param mediator
     *      the associated Mediator
     * @param atts
     *      the set of attributes data structure for the 
     *      xml value element
     */
    public ValueDefinition (Mediator mediator, Attributes atts, 
            TypeDefinition typeDefinition)
    {
        super(mediator, atts);
        this.typeDefinition = typeDefinition;
    }
    
    /**
     * Sets the string value defined in the associated
     * xml element
     * 
     * @param typeDefinition
     *      the string value defined in the associated
     *      xml element
     */
    public void setValue(String value)
    {
       try
       {
           this.value = CastUtils.cast(
        		super.mediator.getClassLoader(),
                   this.typeDefinition.getType(), value);

        } catch (ClassCastException e)
        {
            if(super.mediator.isErrorLoggable())
            {
                super.mediator.error(e, e.getMessage());
            }
        } 
    }    
    
    /**
     * Returns this ValueDefinition's value object 
     *  
     * @return
     *      this ValueDefinition's value object 
     */
    public Object getValue()
    {
        return this.value;
    }

    /**
     * Return the {@link TypeDefinition} wrapping the
     * value object Type of this ValueDefinition
     * 
     * @return
     *      the {@link TypeDefinition} wrapping the
     *      value object Type of this ValueDefinition
     */
    public TypeDefinition getTypeDefinition()
    {
        return this.typeDefinition;
    }
}

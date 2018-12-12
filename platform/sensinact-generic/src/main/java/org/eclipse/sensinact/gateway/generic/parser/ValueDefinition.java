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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.xml.sax.Attributes;

/**
 * A {@link ValueDefinition} defines a simple XML "value" element
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "value", field = "value")
public class ValueDefinition<C> extends XmlModelParsingContext {
    /**
     * the value object defined in the associated XML "value" element
     */
    protected Object value;

    /**
     * the TypeDefinition wrapping the Type of this ValueDefinition
     */
    protected TypeDefinition<C> typeDefinition;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the ValueDefinition to be
     * instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the current 
     * XML node */
    public ValueDefinition(Mediator mediator, Attributes atts, TypeDefinition<C> typeDefinition) {
        super(mediator, atts);
        this.typeDefinition = typeDefinition;
    }

    /**
     * Sets the string value defined in the associated XML "value" element
     *
     * @param value the string value
     */
	public void setValue(String value) {
    	C type = this.typeDefinition.getType();
    	if(Class.class.isAssignableFrom(type.getClass())){
	        try {
	            this.value = CastUtils.cast(super.mediator.getClassLoader(), (Class<?>)type, value);
	        } catch (ClassCastException e) {
	            super.mediator.error(e);
	        }catch(Exception e) {
	        	e.printStackTrace();
	        }
    	} else {
    		this.value = value;
    	}
    }

    /**
     * Returns this ValueDefinition's value object
     *
     * @return this ValueDefinition's value object
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Return the {@link TypeDefinition} associated to this 
     * ValueDefinition
     *
     * @return this ValueDefinition's {@link TypeDefinition}
     */
    public TypeDefinition<?> getTypeDefinition() {
        return this.typeDefinition;
    }
}

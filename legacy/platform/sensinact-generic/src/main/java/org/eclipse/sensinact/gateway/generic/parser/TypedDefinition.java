/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.xml.sax.Attributes;

/**
 * A {@link TypedDefinition} defines an XML node holding a "type" attribute
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({ @XmlAttribute(attribute = "type", field = "type") })
public abstract class TypedDefinition<T> extends XmlModelParsingContext {
	
	public abstract void setType(T type);
	
    /**
     * the Type specified by the associated
     * XML node 's "type" attribute
     */
    protected T type;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the TypedDefinition to be
     * instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the current 
     * XML node */
    public TypedDefinition(Mediator mediator, Attributes atts) {
    	super(mediator,atts);
    }
	
    /**
     * Returns the type specified by the associated
     * XML node 's "type" attribute
     *
     * @return the type of this TypedDefinition
     */
    public T getType() {
        return this.type;
    }
    
    /** 
     * Returns the type specified by the associated
     * XML node 's "type" attribute wrapped into a
     * {@link TypeDefinition} data structure
     * 
     * @return this TypedDefinition's type wrapped into
     * a {@link TypeDefinition}
     */
    public TypeDefinition<T> getTypeDefinition(){
    	return new TypeDefinition<T>(this.type);
    }
}

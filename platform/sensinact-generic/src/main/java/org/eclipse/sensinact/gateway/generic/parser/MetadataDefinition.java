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
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.Metadata;
import org.eclipse.sensinact.gateway.core.MetadataBuilder;


/**
 * Extended {@link NameTypeValueDefinition} for metadata 
 * xml element 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MetadataDefinition extends NameTypeValueDefinition implements
MetadataBuilder
{
    /**
     * Constructor
     * 
     * @param mediator
     *      the associated Mediator
     * @param atts
     *      the set of attributes data structure for the 
     *      xml metadata element
     */
    MetadataDefinition(Mediator mediator, Attributes atts)
    {
        super(mediator, atts);
    }
    
    /**
     * Creates and returns the {@link Metadata} described by 
     * this MetadataDefinition, according to the name of the 
     * target passed as parameter
     * 
     * @return
     *      the {@link Metadata} described by this 
     *      MetadataDefinition, according to the name 
     *      of the target passed as parameter
     *      
     * @see MetadataBuilder#getMetadata(String)
     * 
     * @throws InvalidValueException 
     */
    public Metadata getMetadata() throws InvalidValueException
    {
        Metadata metadata = new Metadata(super.mediator, super.getName(), 
        	typeDefinition.getType(), valueDefinition!=null
            ?valueDefinition.getValue():null, Modifiable.FIXED);
        
        return metadata;
    }
}

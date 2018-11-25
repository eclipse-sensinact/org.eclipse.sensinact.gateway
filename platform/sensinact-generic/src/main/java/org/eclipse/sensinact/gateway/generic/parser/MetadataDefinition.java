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
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.Metadata;
import org.eclipse.sensinact.gateway.core.MetadataBuilder;
import org.xml.sax.Attributes;

/**
 * Extended {@link ResolvedNameTypeValueDefinition} for metadata XML node
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MetadataDefinition extends ResolvedNameTypeValueDefinition implements MetadataBuilder {
	/**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the MetadataDefinition 
     * to be instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the "meta" 
     * XML node 
     */
    MetadataDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Creates and returns the {@link Metadata} described by
     * this MetadataDefinition, according to the name of the
     * target passed as parameter
     *
     * @return the {@link Metadata} described by this
     * MetadataDefinition, according to the name
     * of the target passed as parameter
     * @throws InvalidValueException
     * @see MetadataBuilder#getMetadata(String)
     */
    public Metadata getMetadata() throws InvalidValueException {
        Metadata metadata = new Metadata(super.mediator, super.getName(),super.getType(), 
        	valueDefinition != null ? valueDefinition.getValue() : null, Modifiable.FIXED);
        return metadata;
    }
}

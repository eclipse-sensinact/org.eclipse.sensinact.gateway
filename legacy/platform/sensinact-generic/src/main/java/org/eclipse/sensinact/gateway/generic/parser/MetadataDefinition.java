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
@XmlAttributes({@XmlAttribute(attribute = "modifiable", field = "modifiable")})
public class MetadataDefinition extends ResolvedNameTypeValueDefinition implements MetadataBuilder {

    protected Modifiable modifiable;
    
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
     * Defines the modifiable policy of the {@link Metadata}s
     * based on this MetadataDefinition
     *
     * @param modifiable the string formated Modifiable value
     */
    public void setModifiable(String modifiable) {
        if (modifiable == null) {
            return;
        }
        this.modifiable = Modifiable.valueOf(modifiable);
    }

    /**
     * Returns the modifiable policy of the {@link Metadata}s
     * based on this MetadataDefinition
     *
     * @return modifiable policy of the {@link Metadata} base
     * on this MetadataDefinition
     */
    public Modifiable getModifiable() {
        return this.modifiable;
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
        valueDefinition != null ? valueDefinition.getValue() : null, this.modifiable);
        return metadata;
    }
}

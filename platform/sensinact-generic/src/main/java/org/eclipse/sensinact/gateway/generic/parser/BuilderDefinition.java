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
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.xml.sax.Attributes;

/**
 * Extended {@link XmlModelParsingContext} dedicated to builder xml 
 * elements parsing context
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({
	@XmlAttribute(attribute = "xsi:type", field = "name"),
	@XmlAttribute(attribute = "reference", field = "reference"), 
	@XmlAttribute(attribute = "calculated", field = "subType")})
public abstract class BuilderDefinition extends XmlModelParsingContext implements JSONable {

    private String reference;
    private String subType;
    private String name;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the BuilderDefinition to be
     * instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the builder xml 
     * elements
     */
    BuilderDefinition(Mediator mediator,Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Sets the subtype of this BuilderDefinition
     *
     * @param subType this BuilderDefinition's subtype
     */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * Returns the subtype of this BuilderDefinition
     *
     * @return this BuilderDefinition's subtype
     */
    public String getSubType() {
        return this.subType;
    }

    /**
     * Defines the name of the {@link Resource} targeted by
     * the {@link DynamicParameterValue} described by
     * this BuilderDefinition
     *
     * @param reference the name of the targeted {@link Resource}
     */
    public void setReference(String reference) {
        this.reference = reference.toLowerCase();
    }

    /**
     * Returns the name of the {@link Resource}
     * targeted by the {@link DynamicParameterValue} described by
     * this BuilderDefinition
     *
     * @return the name of the targeted {@link Resource}
     */
    public String getReference() {
        return this.reference;
    }

    /**
     * Defines the name of the type of the {@link DynamicParameterValue}
     * described by this BuilderDefinition
     *
     * @param name the name of the type of the described {@link
     *             DynamicParameterValue}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the type of the {@link DynamicParameterValue}
     * described by this BuilderDefinition
     *
     * @return the name of the type of the described {@link
     * DynamicParameterValue}
     */
    public String getName() {
        return this.name;
    }
}

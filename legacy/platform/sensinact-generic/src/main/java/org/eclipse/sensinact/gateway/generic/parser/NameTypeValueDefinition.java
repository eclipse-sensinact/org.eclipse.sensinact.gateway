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
 * Extended abstract {@link TargetedDefinition} for XML node s gathering a name
 * attribute, as well as types and values elements
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({ @XmlAttribute(attribute = "name", field = "name") })
public abstract class NameTypeValueDefinition<C> extends TypedDefinition<C> {
    
    /**
     * the {@link ValueDefinition} of this NameTypeValueDefinition
     */
    protected ValueDefinition<C> valueDefinition;

    protected String name;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 XML node */
    NameTypeValueDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param target
     * @return
     */
    public ValueDefinition<C> getValueDefinition() {
        return this.valueDefinition;
    }
    
    /**
     * Start of an "value" Element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void valueStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.valueDefinition = new ValueDefinition<C>(this.mediator, atts, this.getTypeDefinition());
        setNext(this.valueDefinition);
    }

}

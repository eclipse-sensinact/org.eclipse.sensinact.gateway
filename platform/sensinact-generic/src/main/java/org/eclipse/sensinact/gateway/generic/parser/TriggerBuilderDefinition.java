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
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.xml.sax.Attributes;

/**
 * Extended {@link XmlDefinition} describing a {@link DynamicParameterValue}
 * of a {@link DynamicParameter}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes(value = {})
public class TriggerBuilderDefinition extends GenericNameTypeValueDefinition implements JSONable{
    
    private ArgumentDefinition argumentDefinition;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts the associated XML definition's {@link Attributes}
     */
    TriggerBuilderDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     *
     * @return this argument
     */
    public Object getArgument() {
        return this.argumentDefinition.getValueDefinition().value;
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        StringBuilder builder = new StringBuilder();

        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_ARGUMENT_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        if(argumentDefinition !=null) {
        	builder.append(JSONUtils.toJSONFormat(getArgument()));
        } else {
        	builder.append("null");
        }
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_BUILDER_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(super.getType());
        builder.append(JSONUtils.QUOTE);
        return builder.toString();
    }

    /**
     * Start of a "reference" Element parsing
     */
    public void argumentStart(Attributes atts) {
    	ArgumentDefinition argumentDefinition = new ArgumentDefinition(mediator,atts);
    	this.argumentDefinition = argumentDefinition;
    	super.setNext(argumentDefinition);
    }
}

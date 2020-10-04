/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended {@link XmlDefinition} describing a {@link DynamicParameterValue}
 * of a {@link DynamicParameter}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes(value = { })
public class ParameterBuilderDefinition extends BuilderDefinition implements ConstrainableDefinition {
	
    private List<ConditionalConstant> conditionalConstants;
    private String parameterName;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml reference element
     */
    ParameterBuilderDefinition(Mediator mediator, String parameterName, Attributes atts) {
        super(mediator, atts);
        this.parameterName = parameterName;
        this.conditionalConstants = new ArrayList<ConditionalConstant>();
    }


    /**
     * Returns the name of the type of the {@link DynamicParameterValue}
     * described by this BuilderDefinition
     *
     * @return the name of the type of the described {@link
     * DynamicParameterValue}
     */
    public String getName() {
    	String name = super.getName().toUpperCase();
    	name = name.substring(0,name.length()-7);
        if ("CALCULATED".equals(name)) {
            return super.getSubType();
        }
        return name;
    }

    /**
     * @inheritDoc
     * 
     * @see org.eclipse.sensinact.gateway.generic.parser.ConstrainableDefinition#addConstraint(org.eclipse.sensinact.gateway.generic.parser.ConstraintDefinition)
     */
    @Override
    public void addConstraint(ConstraintDefinition constraint) {
    	ConditionalConstant conditionalConstant = this.conditionalConstants.get(this.conditionalConstants.size() - 1);
    	conditionalConstant.addConstraint(constraint);
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        StringBuilder builder = new StringBuilder();

        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append("reference");
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(super.getReference());
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(DynamicParameterValue.BUILDER_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append(DynamicParameterValue.BUILDER_TYPE_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(getName());
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(DynamicParameterValue.BUILDER_PARAMETER_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.parameterName);
        builder.append(JSONUtils.QUOTE);

        if (!this.conditionalConstants.isEmpty()) {
            builder.append(JSONUtils.COMMA);

            if (this.conditionalConstants.size() == 1 && this.conditionalConstants.get(0).isUnconditional()) {
                builder.append(JSONUtils.QUOTE);
                builder.append(DynamicParameterValue.BUILDER_CONSTANT_KEY);
                builder.append(JSONUtils.QUOTE);
                builder.append(JSONUtils.COLON);
                builder.append(this.conditionalConstants.get(0).getJSON());

            } else {
                builder.append(JSONUtils.QUOTE);
                builder.append(DynamicParameterValue.BUILDER_CONSTANTS_KEY);
                builder.append(JSONUtils.QUOTE);
                builder.append(JSONUtils.COLON);
                builder.append(JSONUtils.OPEN_BRACKET);
                int index = 0;
                int length = this.conditionalConstants.size();

                for (; index < length; index++) {
                    builder.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
                    builder.append(this.conditionalConstants.get(index).getJSON());
                }
                builder.append(JSONUtils.CLOSE_BRACKET);
            }
        }
        builder.append(JSONUtils.CLOSE_BRACE);
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }

    /**
     * Start of a "constant" Element parsing
     */
    public void constantStart(Attributes atts) {
    	ConditionalConstant constant = new ConditionalConstant(this.mediator,atts);
    	this.conditionalConstants.add(constant);
    	super.setNext(constant);
    }  
}

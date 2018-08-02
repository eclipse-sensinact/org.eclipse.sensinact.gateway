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
@XmlAttributes({@XmlAttribute(attribute = "xsi:type", field = "name"), @XmlAttribute(attribute = "reference", field = "reference"), @XmlAttribute(attribute = "calculated", field = "subType")})
public class BuilderDefinition extends XmlDefinition implements JSONable, ConstrainableDefinition {
    private List<ConditionalConstant> conditionalConstants;
    private String reference;
    private String subType;
    private String parameterName;
    private String name;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml reference element
     */
    BuilderDefinition(Mediator mediator, String parameterName, Attributes atts) {
        super(mediator, atts);
        this.parameterName = parameterName;
        this.conditionalConstants = new ArrayList<ConditionalConstant>();
    }

    /**
     * Sets the sub type of the CALCULATED {@link DynamicParameterValue}s
     * described by this BuilderDefinition
     *
     * @param subType the described CALCULATED {@link DynamicParameterValue}s'
     *                sub type
     */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * Returns the sub type of the CALCULATED {@link DynamicParameterValue}s
     * described by this BuilderDefinition
     *
     * @return the described CALCULATED {@link DynamicParameterValue}s'
     * sub type
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
        this.name = name.substring(0, name.length() - 7).toUpperCase();
    }

    /**
     * Returns the name of the type of the {@link DynamicParameterValue}
     * described by this BuilderDefinition
     *
     * @return the name of the type of the described {@link
     * DynamicParameterValue}
     */
    public String getName() {
        if ("CALCULATED".equals(name)) {
            return this.subType;
        }
        return this.name;
    }

    /**
     * @param target
     * @return
     */
    public TypeDefinition getTypeDefinition(String target) {
        return this.conditionalConstants.get(this.conditionalConstants.size() - 1).getTypeDefinition();
    }

    /**
     * @param typeDefinition
     */
    protected void setTypeDefinition(TypeDefinition typeDefinition) {
        ConditionalConstant conditional = new ConditionalConstant(super.mediator, null);
        conditional.setTypeDefinition(typeDefinition);
        this.conditionalConstants.add(conditional);
    }

    /**
     * @param valueDefinition
     */
    protected void setValueDefinition(ValueDefinition valueDefinition) {
        this.conditionalConstants.get(this.conditionalConstants.size() - 1).setValueDefinition(valueDefinition);
    }

    /**
     * @inheritDoc
     * @see ConstrainableDefinition#
     * addConstraint(ConstraintDefinition)
     */
    @Override
    public void addConstraint(ConstraintDefinition constraint) {
        this.conditionalConstants.get(this.conditionalConstants.size() - 1).addConstraint(constraint);
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
        builder.append(this.reference);
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
}

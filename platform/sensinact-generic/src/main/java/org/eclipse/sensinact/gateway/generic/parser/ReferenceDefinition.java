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
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended {@link XmlDefinition} describing a trigger executed
 * when an associated ActionResource is invocated
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "xsi:type", field = "name"), @XmlAttribute(attribute = "reference", field = "reference"), @XmlAttribute(attribute = "passOn", field = "passOn"), @XmlAttribute(attribute = "index", field = "index"), @XmlAttribute(attribute = "calculated", field = "subType")})
public class ReferenceDefinition extends NameTypeValueDefinition implements JSONable, ConstrainableDefinition {
    private List<ConditionalConstant> conditionalConstants;
    private String reference;

    private String subType;
    private boolean passOn;
    private int index;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml reference element
     */
    ReferenceDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
        this.conditionalConstants = new ArrayList<ConditionalConstant>();
    }

    /**
     * Sets the sub type of the CALCULATED {@link AccessMethodTrigger}s
     * described by this ReferenceDefinition
     *
     * @param subType the described CALCULATED {@link AccessMethodTrigger}s'
     *                sub type
     */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * Returns the sub type of the CALCULATED {@link AccessMethodTrigger}s
     * described by this ReferenceDefinition
     *
     * @return the described CALCULATED {@link AccessMethodTrigger}s'
     * sub type
     */
    public String getSubType() {
        return this.subType;
    }

    /**
     * Defines the name of the {@link StateVariableResource}
     * targeted by the {@link AccessMethodTrigger} described by
     * this ReferenceDefinition
     *
     * @param reference the name of the targeted {@link StateVariableResource}
     */
    public void setReference(String reference) {
        this.reference = reference.toLowerCase();
    }

    /**
     * Returns the name of the {@link StateVariableResource}
     * targeted by the {@link AccessMethodTrigger} described by
     * this ReferenceDefinition
     *
     * @return the name of the targeted {@link StateVariableResource}
     */
    public String getReference() {
        return this.reference;
    }

    /**
     * Defines whether the {@link AccessMethodTrigger} described
     * by this {@link ReferenceDefinition} triggers the update
     * to upper layers or not
     *
     * @param passOn <ul>
     *               <li>true to pass on the change to upper layer</li>
     *               <li>false otherwise</li>
     *               </ul>
     */
    public void setPassOn(String passOn) {
        this.passOn = Boolean.parseBoolean(passOn);
    }

    /**
     * Returns true if the {@link AccessMethodTrigger} described
     * by this {@link ReferenceDefinition} triggers the update
     * to upper layers; returns false otherwise
     *
     * @return <ul>
     * <li>true to pass on the change to upper layer</li>
     * <li>false otherwise</li>
     * </ul>
     */
    public boolean getPassOn() {
        return this.passOn;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name.substring(0, name.length() - 9).toUpperCase();
    }

    /**
     * Defines the index of the parameter on which applies
     * the calculation of the value returned by the {@link
     * AccessMethodTrigger} described by this ReferenceDefinition
     *
     * @param index the index of the parameter on which applies
     *              the calculation
     */
    public void setIndex(String index) {
        this.index = Integer.parseInt(index);
    }

    /**
     * Returns the index of the parameter on which applies
     * the calculation of the value returned by the {@link
     * AccessMethodTrigger} described by this ReferenceDefinition
     *
     * @return the index of the parameter on which applies
     * the calculation
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @param target
     * @return
     */
    public TypeDefinition getTypeDefinition() {
        return this.conditionalConstants.get(this.conditionalConstants.size() - 1).getTypeDefinition();
    }

    /**
     * @param typeDefinition
     */
    @Override
    protected void setTypeDefinition(TypeDefinition typeDefinition) {
        ConditionalConstant conditional = new ConditionalConstant(super.mediator, null);
        conditional.setTypeDefinition(typeDefinition);
        this.conditionalConstants.add(conditional);
    }

    /**
     * @param valueDefinition
     */
    @Override
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
        builder.append(AccessMethodTrigger.TRIGGER_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_TYPE_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        if ("CALCULATED".equals(name)) {
            builder.append(this.subType);

        } else {
            builder.append(name);
        }
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_PASS_ON);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.passOn);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_INDEX_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.index);

        if (!this.conditionalConstants.isEmpty()) {
            builder.append(JSONUtils.COMMA);

            if (this.conditionalConstants.size() == 1 && this.conditionalConstants.get(0).isUnconditional()) {
                builder.append(JSONUtils.QUOTE);
                builder.append(AccessMethodTrigger.TRIGGER_CONSTANT_KEY);
                builder.append(JSONUtils.QUOTE);
                builder.append(JSONUtils.COLON);
                builder.append(this.conditionalConstants.get(0).getJSON());

            } else {
                builder.append(JSONUtils.QUOTE);
                builder.append(AccessMethodTrigger.TRIGGER_CONSTANTS_KEY);
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

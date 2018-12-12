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
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.xml.sax.Attributes;

/**
 * Extended abstract {@link TargetedDefinition} for XML node s gathering a name
 * attribute, as well as types and values elements
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResolvedNameTypeValueDefinition extends NameTypeValueDefinition<Class<?>> {
    
    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 XML node */
    ResolvedNameTypeValueDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     *
     */
    protected final TypeValuePair getTypeValuePair() {
        Class<?> type = super.getType();
        return new TypeValuePair(type, valueDefinition != null ? valueDefinition.getValue() : null);
    }
    
    /**
     * Sets the canonical name of the Type specified
     * by the associated xml tag element
     *
     * @param type the canonical name of the Type specified
     *             by the associated xml tag element
     */
    public void setType(String type) {
        super.type = null;
        try {
            super.type = CastUtils.loadClass(super.mediator.getClassLoader(), type);
        } catch (ClassNotFoundException e) {
            super.mediator.error(new StringBuilder().append("Invalid type : ").append(type).toString());
        }
    }

    /**
     * Defines this TypedDefinition's type
     *
     * @param type this TypedDefinition's type
     */
    public void setType(Class<?> type) {
        super.type = type;
    }
    
    /**
     * Start of a "minExclusive" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void minExclusiveStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "minExclusive");
    }

    /**
     * Start of a "minInclusive" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void minInclusiveStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "minInclusive");
    }

    /**
     * Start of a "maxExclusive" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void maxExclusiveStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "maxExclusive");
    }

    /**
     * Start of a "maxInclusive" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void maxInclusiveStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "maxInclusive");
    }

    /**
     * Start of a "length" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void lengthStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "length");
    }

    /**
     * Start of a "minLength" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void minLengthStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "minLength");
    }

    /**
     * Start of a "maxLength" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void maxLengthStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "maxLength");
    }

    /**
     * Start of a "enumeration" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void enumerationStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "enumeration");
    }

    /**
     * Start of a "fixed" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void fixedStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "fixed");
    }

    /**
     * Start of a "pattern" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void patternStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "pattern");
    }

    /**
     * Start of a "delta" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void deltaStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "delta");
    }

    /**
     * Start of a "absolute" restriction element parsing
     *
     * @throws InvalidXmlDefinitionException
     */
    public void absoluteStart(Attributes atts) throws InvalidXmlDefinitionException {
        this.startConstraint(atts, "absolute");
    }

    public void startConstraint(Attributes atts, String name) throws InvalidXmlDefinitionException {
    	if(ConstrainableDefinition.class.isAssignableFrom(getClass())) {
    		ConstraintDefinition constraintDefinition = new ConstraintDefinition(mediator, name, atts);
    		constraintDefinition.setType(getTypeDefinition());
    		((ConstrainableDefinition)this).addConstraint(constraintDefinition);
    		setNext(constraintDefinition);
        }
    }
}

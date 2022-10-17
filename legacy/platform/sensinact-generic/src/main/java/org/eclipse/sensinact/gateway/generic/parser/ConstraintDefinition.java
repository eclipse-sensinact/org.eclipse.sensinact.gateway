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
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.xml.sax.Attributes;

/**
 * Extended {@link XmlModelParsingContext}  dedicated to "constraint" 
 * XML node parsing context
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({
	@XmlAttribute(attribute = "value", field = "value"), 
	@XmlAttribute(attribute = "reference", field = "reference"), 
	@XmlAttribute(attribute = "complement", field = "complement")})
public class ConstraintDefinition extends XmlModelParsingContext implements JSONable {
    
	private String value;
    private String reference;
    private boolean complement;
    private final String name;

    private TypeDefinition<Class<?>> typeDefinition;
    
    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the ConstraintDefinition to 
     * be instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the "constraint" 
     * XML node
     */
    public ConstraintDefinition(Mediator mediator, String name, Attributes atts) {
        super(mediator, atts);
        this.name = name;
    }

    /**
     * Sets the string formated value of this ConstraintDefinition
     *
     * @param value the string formated value of this ConstraintDefinition
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Defines whether a value submitted to {@link Constraint}
     * described by this ConstraintDefinition has to comply the
     * specified condition or its logical complement
     *
     * @param complement defines if a value has to comply the specified
     * condition or its logical complement
     */
    public void setComplement(String complement) {
        this.complement = Boolean.parseBoolean(complement);
    }

    /**
     * Sets the {@link TypeDefinition} defining the type of the value 
     * on which the described constraint applies 
     *
     * @param typeDefinition the {@link TypeDefinition} applying
     */
    public void setType(TypeDefinition<Class<?>> typeDefinition) {
        this.typeDefinition = typeDefinition;
    }


    /**
     * Sets the string formated reference of this ConstraintDefinition if any
     *
     * @param reference the string formated reference of this ConstraintDefinition
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Returns the {@link Constraint} described by this ConstraintDefinition
     *
     * @param clazz the type of the value on which the described constraint applies
     * @return the {@link Constraint} described by this ConstraintDefinition
     * 
     * @throws InvalidConstraintDefinitionException
     */
    public Constraint getConstraint(Class<?> clazz) throws InvalidConstraintDefinitionException {
        Constraint constraint = ConstraintFactory.Loader.load(
    		super.mediator.getClassLoader(), 
    		this.name, 
    		clazz, 
    		(reference != null)?new Object[]{this.value, this.reference}:this.value, this.complement);
        return constraint;
    }

    /**
     * @inheritDoc
     * 
     * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append(Constraint.OPERATOR_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.name);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(Constraint.TYPE_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(CastUtils.writeClass(this.typeDefinition.getType()));
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(Constraint.OPERAND_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);

        if (this.reference != null) {
            builder.append(JSONUtils.OPEN_BRACKET);
            builder.append(JSONUtils.toJSONFormat(this.value));
            builder.append(JSONUtils.COMMA);
            builder.append(JSONUtils.toJSONFormat(this.reference));
            builder.append(JSONUtils.CLOSE_BRACKET);

        } else {
            builder.append(JSONUtils.toJSONFormat(this.value));
        }
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(Constraint.COMPLEMENT_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.complement);
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }
}

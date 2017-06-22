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

import org.xml.sax.Attributes;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * A ConstraintDefinition described a {@link Restriction} which applies
 * on an access method parameter
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute ="value", field ="value"),
	@XmlAttribute(attribute ="reference", field ="reference"),
	@XmlAttribute(attribute ="complement", field ="complement")})
public class ConstraintDefinition extends XmlDefinition implements JSONable
{
	private String value;
	private String reference;
	private boolean complement;
	
	private TypeDefinition typeDefinition;
	
	private final String name;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the associated {@link Mediator}
	 * @param atts
	 * 		the restriction xml element's set of attributes
	 */
	public ConstraintDefinition(Mediator mediator, 
			String name, Attributes atts)
	{
		super(mediator, atts);
		this.name = name;
	}

	/**
	 * Sets the string formated value of this
	 * ConstraintDefinition
	 * 
	 * @param value
	 *      the string formated value of this
     *      ConstraintDefinition
	 */
	public void setValue(String value)
	{
	   this.value = value;
	}

	/**
	 * Defines whether a value submitted to {@link Constraint} 
	 * described by this ConstraintDefinition has to comply the 
	 * specified condition or its logical complement 
	 * 
	 * @param complement
	 * 		defines if a value has to comply the specified 
	 * 		condition or its logical complement
	 */
	public void setComplement(String complement)
	{
	   this.complement = Boolean.parseBoolean(complement);
	}

	/**
	 * Sets the {@link TypeDefinition} defining the string 
	 * formated value of this ConstraintDefinition
	 * 
	 * @param value
	 *      the string formated value of this
     *      ConstraintDefinition
	 */
	public void setType(TypeDefinition typeDefinition)
	{
	   this.typeDefinition = typeDefinition;
	}
	
	/**
	 * Sets the string formated reference of this
	 * ConstraintDefinition
	 * 
	 * @param reference
	 *      the string formated reference of this
     *      ConstraintDefinition
	 */
	public void setReference(String reference)
	{
	   this.reference = reference;
	}
	
	/**
	 * Returns the {@link Constraint} defined by this 
	 * ConstraintDefinition
	 * 
	 * @param clazz
	 *      the type of the value on which the restriction
	 *      applies
	 * @return
	 * 		the {@link Constraint} defined by 
	 * 		this ConstraintDefinition
	 * 
	 * @throws InvalidConstraintDefinitionException 
	 */
	public Constraint getConstraint(Class<?> clazz) 
			throws InvalidConstraintDefinitionException
	{
		Constraint constraint = ConstraintFactory.Loader.load(
			super.mediator.getClassLoader(), this.name,  clazz, 
		    (reference!=null)?new Object[]{this.value, this.reference}
			:this.value, this.complement);
		
		return constraint;
	}

	/**
	 * @inheritDoc
	 *
	 * @see JSONable#getJSON()
	 */
    @Override
    public String getJSON()
    {
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
	    builder.append(CastUtils.writeClass(
	    		this.typeDefinition.getType()));
	    builder.append(JSONUtils.QUOTE);
	    builder.append(JSONUtils.COMMA);
	    builder.append(JSONUtils.QUOTE);
	    builder.append(Constraint.OPERAND_KEY);
	    builder.append(JSONUtils.QUOTE);
	    builder.append(JSONUtils.COLON);
	    
	    if(this.reference !=null)
	    {
	    	builder.append(JSONUtils.OPEN_BRACKET);
	    	builder.append(JSONUtils.toJSON(this.value));
		    builder.append(JSONUtils.COMMA);
		    builder.append(JSONUtils.toJSON(this.reference));
	    	builder.append(JSONUtils.CLOSE_BRACKET);
	    	
	    } else
	    {
	    	builder.append(JSONUtils.toJSON(this.value));
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

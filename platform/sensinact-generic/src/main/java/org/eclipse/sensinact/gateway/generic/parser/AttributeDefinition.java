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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.AttributeBuilder;
import org.eclipse.sensinact.gateway.core.RequirementBuilder;
import org.eclipse.sensinact.gateway.core.AttributeBuilder.Requirement;
import org.eclipse.sensinact.gateway.core.MetadataBuilder;

/**
 * Extended {@link NameTypeValueDefinition} for attribute 
 * xml element 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "modifiable", field = "modifiable"),
				@XmlAttribute(attribute = "hidden", field = "hidden")})
public class AttributeDefinition extends TargetedNameTypeValueDefinition implements 
ConstrainableDefinition
{
	private List<ConstraintDefinition> constraintDefinitions;
	private List<MetadataBuilder> metadataDefinitions;
    
    protected Modifiable modifiable;
    protected boolean hidden;
    
    /**
     * 
     * Constructor
     * 
     * @param mediator
     *      the associated Mediator
     * @param atts
     *      the set of attributes data structure for the 
     *      xml attribute element
     */
    AttributeDefinition(Mediator mediator, Attributes atts)
    {
        super(mediator, atts);
        this.constraintDefinitions = new ArrayList<ConstraintDefinition>();
        this.metadataDefinitions = new ArrayList<MetadataBuilder>();
    }

    /**
     * Defines the modifiable policy of the {@link Attribute}s 
     * based on this AttributeDefinition
     * 
     * @param modifiable
     * 		the string formated Modifiable value
     */
    public void setModifiable(String modifiable)
    {
    	if(modifiable == null)
    	{
    		return;
    	}
    	this.modifiable=Modifiable.valueOf(modifiable);
    }
    
    /**
     * Defines whether the {@link Attribute}s based on this
     * AttributeDefinition are hidden 
     * 
     * @param hidden
     * 		the string formated boolean value
     */
    public void setHidden(String hidden)
    {
    	this.hidden=hidden==null
    			?false:Boolean.parseBoolean(hidden);
    }
    
    /**
     * Returns the modifiable policy of the {@link Attribute}s 
     * based on this AttributeDefinition
     * 
     * @return
     * 		modifiable policy of the {@link Attribute} base
     * 		on this AttributeDefinition
     */
	public Modifiable getModifiable()
	{
		return this.modifiable;
	}

	 /**
     * Returns true if the {@link Attribute}s based on this
     * AttributeDefinition are hidden ; Otherwise returns
     * false
     * 
     * @return
     * 		visible state of the {@link Attribute} base
     * 		on this AttributeDefinition
     */
	public boolean isHidden()
	{
		return this.hidden;
	}
	
    /**
     * @inheritDoc
     * 
     * @see ConstrainableDefinition#
     * addConstraint(ConstraintDefinition)
     */
    public void addConstraint(ConstraintDefinition constraint)
    {
    	if(constraint != null)
    	{
    		this.constraintDefinitions.add(constraint);
    	}
    }
 
    /**
     * 
     */
    public void addMetadataBuilder(MetadataDefinition metadata)
    {
    	if(metadata != null)
    	{
    		this.metadataDefinitions.add(metadata);
    	}
    }
	
    /**
     * @return
     */
    public List<MetadataBuilder> getMetadataBuilders()
    {
    	return Collections.unmodifiableList(this.metadataDefinitions);
    }
    
	/**
	 * Converts this AttributeDefinition into a set of 
	 * {@link RequirementBuilder} and returns it as a list
	 * 
	 * @return
	 * 		the list of {@link RequirementBuilder}s this 
	 * 		AttributeDefinition describes
	 */
	public List<RequirementBuilder> getRequirementBuilders(String service) 
	{
		List<RequirementBuilder> requirementBuilders = 
				new ArrayList<RequirementBuilder>();
			
		TypeValuePair nameTypePair = this.getTypeValuePair(service);			
        
		if(nameTypePair != null && nameTypePair.type != null)
		{
			RequirementBuilder requirementBuilder = null;
			
			if(this.modifiable!=null)
			{
				requirementBuilder = new RequirementBuilder(
				Requirement.MODIFIABLE, this.getName());
				requirementBuilder.put(service, this.modifiable);
				requirementBuilders.add(requirementBuilder);
			}
			requirementBuilder = new RequirementBuilder(
					Requirement.HIDDEN, this.getName());
			requirementBuilder.put(service, this.hidden);
			requirementBuilders.add(requirementBuilder);
			requirementBuilder = new RequirementBuilder(
					Requirement.TYPE, this.getName());
			requirementBuilder.put(service, nameTypePair.type);
			requirementBuilders.add(requirementBuilder);
			requirementBuilder = new RequirementBuilder(
					Requirement.VALUE, this.getName());
			requirementBuilder.put(service, nameTypePair.value);
			requirementBuilders.add(requirementBuilder);
		}
        return requirementBuilders;
	}    

	/**
	 * Converts this AttributeDefinition into an {@link AttributeBuilder} 
	 * returns it
	 * 
	 * @return
	 * 		the {@link AttributeBuilder}s this AttributeDefinition 
	 * 		describes
	 */
	public AttributeBuilder getAttributeBuilder(String service) 
	{
		TypeValuePair nameTypePair = this.getTypeValuePair(service);	
        
        if(nameTypePair==null || nameTypePair.type==null)
        {
        	return null;
        }
        AttributeBuilder attributeBuilder = new AttributeBuilder(
        		this.getName(), new Requirement[]{Requirement.TYPE, 
        			Requirement.MODIFIABLE, Requirement.HIDDEN});

        attributeBuilder.type(nameTypePair.type);
        if(nameTypePair.value != null)
        {
        	attributeBuilder.value(nameTypePair.value);
        }
        if(this.modifiable != null)
        {
        	attributeBuilder.modifiable(this.modifiable);
        }
        attributeBuilder.hidden(this.hidden);  
        
        List<Constraint> constraints = this.getConstraints(service);
        if(constraints != null)
        {
        	attributeBuilder.addConstraints(constraints);
        }
        attributeBuilder.addMetadataBuilders(this.metadataDefinitions);
        return attributeBuilder;
	}
	
	/**
	 * Converts the list {@link ConstraintDefinition}s of this
	 * AttributeDefinition into a the list of the appropriate 
	 * list of {@link Constraint}s applying on an @link Attribute} 
	 * based on this AttributeDefinition and returns it
	 * 
	 * @param service
	 * 		the name of the service holding the resource for 
	 * 		which the AttributeDefinition has been created
	 * @return		
	 * 		the list of {@link Constraint}s applying on an 
	 * 		{@link Attribute} based on this AttributeDefinition
	 */
	public List<Constraint> getConstraints(String service) 
	{
		TypeValuePair nameTypePair = this.getTypeValuePair(service);	
        
        if(nameTypePair==null || nameTypePair.type==null)
        {
        	return Collections.<Constraint>emptyList();
        }
        List<Constraint> constraints = new ArrayList<Constraint>();
        
        Iterator<ConstraintDefinition> iterator = 
        	this.constraintDefinitions.iterator();
        
        while(iterator.hasNext())
        {
        	ConstraintDefinition definition = iterator.next();
        	try
            {
	            constraints.add(definition.getConstraint(
	            	nameTypePair.type));
            }
            catch (InvalidConstraintDefinitionException e)
            {
	            if(super.mediator.isErrorLoggable())
	            {
	            	super.mediator.error(e,e.getMessage());
	            }
            }
        }
        return constraints;
	}
}

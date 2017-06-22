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
package org.eclipse.sensinact.gateway.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * Data structure gathering needed to complete an 
 * {@link AttributeBuilder.Requirement}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RequirementBuilder implements Nameable, 
Iterable<Map.Entry<String,Object>>
{
    private final AttributeBuilder.Requirement requirement;
	private final String attributeName;
	private final Map<String , Object> values;
	
	/**
	 * Constructor
	 * 
	 * @param requirement
	 * @param attributeName
	 */
	public RequirementBuilder(AttributeBuilder.Requirement requirement,
	String attributeName)
	{
		this.requirement = requirement;
		this.attributeName = attributeName;
		values = new HashMap<String, Object>();
	}
	
	/**
	 * @param service
	 * @param value
	 */
	public void put(String service, Object value)
	{
		if(service == null ||service.length()==0)
		{
			this.put(value);
			
		} else
		{
			this.values.put(service, value);
		}
	}	

	/**
	 * @param value
	 */
	public void put(Object value)
	{
		this.values.put(ResourceConfig.ALL_TARGETS, value);
	}
	
	/**
	 * @param service
	 * @return
	 */
	public Object get(String service)
	{
		Object value = this.values.get(service);
		if(value == null && service.intern()
				!=ResourceConfig.ALL_TARGETS.intern())
		{
			value = this.values.get(ResourceConfig.ALL_TARGETS);
		}
		return value;
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Map.Entry<String,Object>> iterator()
	{
		return this.values.entrySet().iterator();
	}
	
	/**
	 * Applies this RequirementBuilder on the set of 
	 * {AttributeBuilder}s passed as parameter, meaning 
	 * sets the value of the one targeting the same
	 * attribute and the same requirement
	 * 
	 * @param builders
	 */
	public void apply(String service, List<AttributeBuilder> builders)
	{				
		 int index = builders.indexOf(new Name<AttributeBuilder>(
				 this.attributeName));
		 if(index == -1)
		 {
			 return;
		 }
		 this.apply(service, builders.get(index));
	}
	
	/**
	 * Applies this RequirementBuilder on the set of 
	 * {AttributeBuilder}s passed as parameter, meaning 
	 * sets the value of the one targeting the same
	 * attribute and the same requirement
	 * 
	 * @param builders
	 */
	public void apply(String service, AttributeBuilder builder)
	{				
		 Object value = this.get(service);
		 if(value == null || !this.attributeName.equals(builder.getName()))
		 {
			 return;
		 } 
		 switch(this.requirement)
		 {
			case HIDDEN:
				builder.hidden((Boolean)value);
				break;
			case MODIFIABLE:
				builder.modifiable((Modifiable)value);
				break;
			case TYPE:
				builder.type((Class)value);
				break;
			case VALUE:
				builder.value(value);
				break;
			default:
				break;
		}
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object)
	{
		if(object==null)
		{
			return false;
		}
		Class objectClass = object.getClass();
		if(RequirementBuilder.class.isAssignableFrom(
				objectClass))
		{
			RequirementBuilder builder = (
					RequirementBuilder)object;
			
			return this.equals(builder.attributeName)
					&& this.equals(builder.requirement);
			
		} else if(String.class == objectClass)
		{
			return object.equals(this.attributeName);
			
		} else if(AttributeBuilder.Requirement.class.isAssignableFrom(
				objectClass))
		{
			return object.equals(this.requirement);
		}
		return false;
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() 
	{
		return this.attributeName;
	}
}

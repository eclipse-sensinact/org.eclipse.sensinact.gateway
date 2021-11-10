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
package org.eclipse.sensinact.gateway.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * Data structure gathering needed to complete an {@link AttributeBuilder.Requirement}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RequirementBuilder implements Nameable, Iterable<Map.Entry<String, Object>> {
	
	protected final AttributeBuilder.Requirement requirement;
	protected final String attributeName;
	protected final Map<String, Object> values;

	/**
	 * Constructor
	 * 
	 * @param requirement {@link AttributeBuilder.Requirement} to be built 
	 * @param attributeName the String name of the {@link Attribute} for which the Requirement is built
	 */
	public RequirementBuilder(AttributeBuilder.Requirement requirement, String attributeName) {
		this.requirement = requirement;
		this.attributeName = attributeName;
		this.values = new HashMap<String, Object>();
	}
	
	/**
	 * @param value
	 */
	public void put(Object value) {
		this.put(ResourceConfig.ALL_TARGETS, value);
	}

	/**
	 * @param service
	 * @param value
	 */
	public void put(String service, Object value) {
		Object val = (value!=null && value.getClass()==String.class)?new StringPatternValue((String)value):value;
		if (service == null || service.length() == 0) 
			this.values.put(ResourceConfig.ALL_TARGETS, val);
		else 
			this.values.put(service, val);
	}

	/**
	 * @param service
	 * @return
	 */
	public Object get(String service) {
		Object value = this.values.get(service);
		if (value == null && service.intern() != ResourceConfig.ALL_TARGETS.intern())
			value = this.values.get(ResourceConfig.ALL_TARGETS);
		return value;
	}
	
	/**
	 * Applies this RequirementBuilder on the set of {AttributeBuilder}s passed as
	 * parameter, meaning sets the value of the one targeting the same attribute and
	 * the same requirement
	 * 
	 * @param service
	 * @param builders
	 */
	public void apply(String service, List<AttributeBuilder> builders) {
		builders.stream()
			.filter(b -> this.attributeName.equals(b.getName()))
			.findFirst()
			.ifPresent(b -> this.apply(service, b));
	}

	/**
	 * Applies this RequirementBuilder on the set of {AttributeBuilder}s passed as
	 * parameter, meaning sets the value of the one targeting the same attribute and
	 * the same requirement
	 * 
	 * @param service
	 * @param builders
	 */
	public void apply(String service, AttributeBuilder builder) {
		Object value = this.get(service);
		if (value == null || !this.attributeName.equals(builder.getName())) 
			return;
		if(value instanceof StringPatternValue)
			value = this.attributeName.equals(Resource.NAME)?((StringPatternValue)value).getLast()
					:((StringPatternValue)value).build();
		switch (this.requirement) {
			case HIDDEN:
				builder.hidden((Boolean) value);
				break;
			case MODIFIABLE:
				builder.modifiable((Modifiable) value);
				break;
			case TYPE:
				builder.type((Class<?>) value);
				break;
			case VALUE:
				builder.value(value);
				break;
			default:
				break;
		}
	}

	@Override
	public Iterator<Map.Entry<String, Object>> iterator() {
		return this.values.entrySet().iterator();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) 
			return false;
		Class<?> objectClass = object.getClass();
		if (RequirementBuilder.class.isAssignableFrom(objectClass)) {
			RequirementBuilder builder = (RequirementBuilder) object;
			return this.equals(builder.attributeName) && this.equals(builder.requirement);
		} else if (String.class == objectClass) 
			return object.equals(this.attributeName);
		else if (AttributeBuilder.Requirement.class.isAssignableFrom(objectClass))
			return object.equals(this.requirement);
		return false;
	}

	@Override
	public String getName() {
		return this.attributeName;
	}
}

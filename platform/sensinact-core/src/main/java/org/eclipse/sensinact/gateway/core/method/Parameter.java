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
package org.eclipse.sensinact.gateway.core.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Metadata;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueTypeException;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.Primitive;
import org.eclipse.sensinact.gateway.common.primitive.PrimitiveDescription;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * A Parameter of an {@link AccessMethod} on which can apply set of constraints
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Parameter extends Primitive implements JSONable, Cloneable {
	public static final String FIXED_PARAMETER_KEY = "fixed";

	protected boolean fixed = false;
	protected Object fixedValue = null;
	protected List<Constraint> constraints;
	
	/**
	 * Constructor
	 * 
	 * @param mediator {@link Mediator} allowing the Parameter to be created to interact 
	 * with the OSGi host environment
	 * @param name the String name of the Parameter to be instantiated
	 * @param type the java type of the Parameter to be instantiated
	 * 
	 * @throws InvalidValueException
	 */
	public Parameter(Mediator mediator, String name, Class<?> type) throws InvalidValueException {
		super(mediator, name, type);
		this.fixed = false;
		this.fixedValue = null;
		this.constraints = Collections.<Constraint>emptyList();
	}

	/**
	 * Constructor
	 * 
	 * @param mediator {@link Mediator} allowing the Parameter to be created to interact 
	 * with the OSGi host environment
	 * @param name the String name of the Parameter to be instantiated
	 * @param type the java type of the Parameter to be instantiated
	 * @param constraints the {@link List} of {@link Constaint}s applying on the Parameter to be instantiated
	 * 
	 * @throws InvalidValueException
	 */
	public Parameter(Mediator mediator, String name, Class<?> type, List<Constraint> constraints)
			throws InvalidValueException {
		super(mediator, name, type);
		this.fixed = false;
		this.fixedValue = null;
		this.constraints = Collections.unmodifiableList(new ArrayList<Constraint>(constraints));
	}

	/**
	 * Constructor
	 * 
	 * 
	 * @param mediator {@link Mediator} allowing the Parameter to be created to interact 
	 * with the OSGi host environment
	 * @param name the String name of the Parameter to be instantiated
	 * @param type the java type of the Parameter to be instantiated
	 * @param value the fixed Object value of the Parameter to be instantiated
	 * 
	 * @throws InvalidValueException
	 */
	public Parameter(Mediator mediator, String name, Class<?> type, Object value) throws InvalidValueException {
		super(mediator, name, type, value);
		this.fixed = true;
		this.fixedValue = value;
		this.constraints = Collections.emptyList();
	}

	/**
	 * Constructor
	 * 
	 * @param mediator {@link Mediator} allowing the Parameter to be created to interact 
	 * with the OSGi host environment
	 * @param parameter the {@link JSONObject} describing the Parameter to be instantiated
	 * 
	 * @throws InvalidValueException
	 */
	public Parameter(Mediator mediator, JSONObject parameter) throws InvalidValueException {
		super(mediator, parameter);
		this.fixed = parameter.optBoolean(FIXED_PARAMETER_KEY);	
		if (fixed) {
			this.fixedValue = parameter.opt(DataResource.VALUE);
			this.constraints = Collections.emptyList();
		} else {
			this.fixedValue = null;
			JSONArray constraints = parameter.optJSONArray(Metadata.CONSTRAINTS);
			List<Constraint> constraintList = new ArrayList<Constraint>();
			int index = 0;
			int length = constraints == null ? 0 : constraints.length();
			try {
				for (; index < length; index++) {
					JSONObject constraintJSON = constraints.getJSONObject(index);
					Constraint constraint = ConstraintFactory.Loader.load(super.mediator.getClassLoader(),constraintJSON);
					constraintList.add(constraint);
				}
			} catch (InvalidConstraintDefinitionException e) {
				throw new InvalidValueException(e);
			}
			this.constraints = Collections.unmodifiableList(constraintList);
		}
	}

	/**
	 * Returns true if the value of the {@link Parameter} argument can be set to
	 * this one ; also checks whether its name is the same as the current one if the
	 * 'strict' argument is defined to true; returns false otherwise
	 * 
	 * @param parameter
	 *            the {@link Parameter} to evaluate
	 * @return
	 *         <ul>
	 *         <li>true if the parameter argument value can be set to this one</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean validParameter(Parameter parameter, boolean strict) {
		boolean complies = (this.validParameter(parameter.getValue())
				&& (!strict || super.getName().equals(parameter.getName())));
		return complies;
	}

	/**
	 * Returns true if the value of the parameter argument can be set to this
	 * Parameter ;
	 * 
	 * @param parameter
	 *            the {@link Parameter} to evaluate
	 * @return
	 *         <ul>
	 *         <li>true if the parameter argument value can be set to this one</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean validParameter(Parameter parameter) {
		return this.validParameter(parameter, false);
	}

	/**
	 * Returns true if the JSON object parameter's value can be set as value of this
	 * Parameter ;
	 * 
	 * @param parameter
	 *            the {@link Parameter} to evaluate
	 * @return
	 *         <ul>
	 *         <li>true if the parameter argument value can be set to this one</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean validParameter(JSONObject jsonParameter) {
		try {
			Parameter parameter = new Parameter(super.mediator, jsonParameter);
			this.validParameter(parameter, false);

		} catch (InvalidValueException e) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if the object argument can be set as value of this Parameter;
	 * 
	 * @param parameter
	 *            the object to evaluate
	 * @return
	 *         <ul>
	 *         <li>true if the object argument can be set as value of this one</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean validParameter(Object parameter) {
		if (Modifiable.FIXED.equals(this.getModifiable())) 
			return true;
		if((parameter == null || (parameter.getClass().isArray() 
				&& parameter.getClass().getComponentType() == Object.class)))
			return true;
		Iterator<Constraint> iterator = this.constraints.iterator();
		while (iterator.hasNext()) {
			Constraint constraint = iterator.next();
			if (!constraint.complies(parameter)) 
				return false;
		}
		try {
			super.setValue(parameter);
		} catch (InvalidValueException e) {
			return false;
		}
		return true;
	}

	@Override
	public Modifiable getModifiable() {
		return !this.fixed ? Modifiable.UPDATABLE : Modifiable.FIXED;
	}
	
	public void reset() {
		if (Modifiable.FIXED.equals(this.getModifiable())) 
			return;
		try {
			super.setValue(this.fixedValue);
		} catch (InvalidValueException e) {
			mediator.error(e);
		}
	}

	@Override
	public Object getValue() {
		return super.value;
	}

	@Override
	public String getJSON() {
		JSONObject description = getJSONObject();
		if (String.class == this.getType() || this.getType().isPrimitive()) 
			description.put(PrimitiveDescription.VALUE_KEY, this.getValue());
		else
			description.put(PrimitiveDescription.VALUE_KEY, JSONUtils.toJSONFormat(this.getValue()));
		return description.toString();
	}
	
	/**
	 * Returns the JSONObject from which is based the JSON formated string
	 * description of this Parameter
	 * 
	 * @return the basis JSONObject describing this Parameter
	 */
	protected final JSONObject getJSONObject() {
		JSONObject description = new JSONObject();
		description.put(PrimitiveDescription.NAME_KEY, name);
		String typeName = CastUtils.writeClass(this.type);

		description.put(PrimitiveDescription.TYPE_KEY, typeName);
		description.put(FIXED_PARAMETER_KEY, this.fixed);
		
		JSONArray constraints = new JSONArray();
		Iterator<Constraint> iterator = this.constraints.iterator();
		while (iterator.hasNext()) {
			Constraint constraint = iterator.next();
			constraints.put(new JSONObject(constraint.getJSON()));
		}
		description.put(Metadata.CONSTRAINTS, constraints);
		return description;
	}

	@Override
	protected final void checkType(Class<?> type) throws InvalidValueTypeException {
		if (Object.class != type && Object[].class != type && !Recipient.class.isAssignableFrom(type)
				&& !Set.class.isAssignableFrom(type)) {
			super.checkType(type);
		}
	}

	@Override
	protected void beforeChange(Object value) throws InvalidValueException {
		// do nothing
	}

	@Override
	protected void afterChange(Object value) throws InvalidValueException {
		// do nothing
	}

	@Override
	public Object clone() {
		try {
			if (this.constraints.isEmpty()) {
				if (this.fixed) 
					return new Parameter(super.mediator, this.name, this.type, this.fixedValue);
				else 
					return new Parameter(super.mediator, this.name, this.type);
			} else 
				return new Parameter(super.mediator, this.name, this.type, this.constraints);
			
		} catch (InvalidValueException e) {
			e.printStackTrace();
			return null;
		}
	}
}

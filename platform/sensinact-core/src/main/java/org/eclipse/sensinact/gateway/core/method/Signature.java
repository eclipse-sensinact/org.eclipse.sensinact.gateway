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

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Signature of an {@link AccessMethod}
 * 
 * @param <T>
 *            the type of the described {@link AccessMethod}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Signature implements JSONable, Iterable<Parameter>, Cloneable {
	/**
	 * the queue of owned parameters
	 */
	protected Parameter[] parameters;

	/**
	 * the type of the associated {@link SnaObjectMethod}
	 */
	protected final String name;

	/**
	 * The {@link Mediator} used to interact with the OSGi host environment
	 */
	protected final Mediator mediator;

	/**
	 * The {@link AccessMethodResponse.Response} defining the Type of the response
	 * of an {@link AccessMethod} based on that Signature
	 */
	protected final AccessMethodResponse.Response returnedType;

	/**
	 * Constructor
	 * 
	 * @param methodType
	 *            the type of the associated {@link SnaObjectMethod}
	 * @param parameters
	 *            this Signature {@link Parameter}s array
	 */
	public Signature(Mediator mediator, String methodType, Parameter[] parameters) {
		this.mediator = mediator;
		int length = parameters == null ? 0 : parameters.length;
		this.parameters = new Parameter[length];
		int index = 0;
		for (; index < length; index++) {
			this.parameters[index] = parameters[index];
		}
		this.name = methodType;
		this.returnedType = AccessMethod.Type.valueOf(this.name).getReturnedType();
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the type of the {@link AccessMethod} associates to the signature
	 *            to instantiate
	 * @param parameterTypes
	 *            the array of parameter types of the signature to instantiate
	 * @param parameterNames
	 *            the array of parameter names of the signature to instantiate
	 * @throws InvalidValueException
	 */
	public Signature(Mediator mediator, AccessMethod.Type type, Class<?>[] parameterTypes, String[] parameterNames)
			throws InvalidValueException {
		this.mediator = mediator;
		String name = null;
		int index = 0;
		int length = parameterTypes == null ? 0 : parameterTypes.length;

		this.parameters = new Parameter[length];

		String[] names = parameterNames == null ? new String[0] : parameterNames;

		for (; index < length; index++) {
			if (index < names.length) {
				name = names[index];
			}
			if (name == null) {
				name = new StringBuilder().append("arg").append(index).toString();
			}
			this.parameters[index] = new Parameter(this.mediator, name, parameterTypes[index]);

			name = null;
		}
		this.name = type.name();
		this.returnedType = AccessMethod.Type.valueOf(this.name).getReturnedType();
	}

	protected Signature(Mediator mediator, String name, AccessMethodResponse.Response returnedType,
			Parameter[] parameters) {
		this.mediator = mediator;
		this.name = name;
		this.returnedType = returnedType;
		this.parameters = parameters.clone();
	}

	/**
	 * Returns the name of the described {@link AccessMethod}
	 * 
	 * @return the name of the described {@link AccessMethod}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the extended {@link SnaMessageType} of the associated
	 * {@link SnaObjectMethod} invocation response object
	 * 
	 * @return the associated {@link AccessMethod} invocation response type
	 */
	public AccessMethodResponse.Response getResponseType() {
		return this.returnedType;
	}

	/**
	 * Returns the parameter types array of the method this Signature reifies the
	 * signature of
	 * 
	 * @return the parameter types array of the method
	 */
	public Class<?>[] getParameterTypes() {
		int index = 0;

		Class<?>[] parameterTypes = new Class<?>[this.length()];
		for (; index < this.length(); index++) {
			parameterTypes[index] = this.parameters[index].getType();
		}
		return parameterTypes;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		Class<?> objectClass = object.getClass();
		if (Signature.class.isAssignableFrom(objectClass)) {
			return this.equals((Signature) object);
		}
		return false;
	}

	/**
	 * Returns true if the name and parameter types of the Signature passed as
	 * parameter are respectively equal to the name and parameter types of this one;
	 * returns false otherwise
	 * 
	 * @param method
	 *            the Signature to test the signature of
	 * @return true if the name and parameter types of the Signature are
	 *         respectively equal to the name and parameter types of this one ;
	 *         returns false otherwise
	 */
	public boolean equals(Signature signature) {
		return this.equals(signature.getName(), signature.getParameterTypes());
	}

	/**
	 * Returns true if the name and parameter types passed as parameters are
	 * respectively equal to the name and parameter types of this Signature; returns
	 * false otherwise
	 * 
	 * @param name
	 *            the name to compare with the one of this Signature
	 * @param parameterTypes
	 *            the parameter types array to compare with the one of this
	 *            Signature
	 * @return true if the name and parameter types are respectively equal to the
	 *         name and parameter types of this Signature ; returns false otherwise
	 */
	public boolean equals(String name, Class<?>[] parameterTypes) {
		int length = parameterTypes == null ? 0 : parameterTypes.length;

		if (this.length() != length || !this.getName().equals(name)) {
			return false;
		}
		Class<?>[] thisParameterTypes = this.getParameterTypes();
		int index = 0;

		for (; index < length && thisParameterTypes[index] == parameterTypes[index]; index++)
			;

		return (index == length);
	}

	/**
	 * Returns the {@link Parameter} whose name is passed as parameter
	 * 
	 * @param name
	 *            the name of the parameter to return
	 * @return the {@link Parameter} whose name is specified
	 */
	public Parameter get(String name) {
		Parameter parameter = null;
		Iterator<Parameter> iterator = this.iterator();

		while (iterator.hasNext()) {
			parameter = iterator.next();
			if (parameter.getName().equals(name)) {
				break;
			}
			parameter = null;
		}

		return parameter;
	}

	/**
	 * Returns the {@link Parameter} whose index is passed as parameter
	 * 
	 * @param index
	 *            the index of the parameter to return
	 * @return the {@link Parameter} whose index is specified
	 */
	public Parameter get(int index) {
		int position = 0;
		Parameter parameter = null;
		Iterator<Parameter> iterator = this.iterator();

		while (iterator.hasNext()) {
			parameter = iterator.next();
			if (position == index) {
				break;
			}
			position++;
			parameter = null;
		}
		return parameter;
	}

	/**
	 * Returns the array of object values of this Signature's set of
	 * {@link Parameter}s
	 * 
	 * @return the array of this Signature's {@link Parameter}s'object values
	 */
	public Object[] values() {
		int position = 0;
		Object[] values = new Object[this.length()];

		Parameter parameter = null;
		Iterator<Parameter> iterator = this.iterator();

		while (iterator.hasNext()) {
			parameter = iterator.next();
			values[position++] = parameter.getValue();
		}
		return values;
	}

	/**
	 * Returns true if the array of {@link Parameter}s passed as parameter contains
	 * {@link Parameter}s of the same type and in the same order than this current
	 * {@link Set} ; returns false otherwise
	 * 
	 * @param methodParameters
	 *            the array of {@link Parameter}s to evaluate the order and the
	 *            types of
	 * @return true if the array of {@link MethodParameter}s passed as parameter
	 *         contains parameters of the same type and in the same order than this
	 *         current {@link Set}; false otherwise
	 */
	public boolean validParameters(Parameter[] methodParameters) {
		int length = methodParameters == null ? 0 : methodParameters.length;

		if (this.parameters.length != length) {
			return false;
		}
		int index = 0;
		for (; index < length; index++) {
			this.parameters[index].reset();

			if (!this.parameters[index].validParameter(methodParameters[index])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the objects array passed as parameter contains valid object
	 * to set as values of held {@link Parameter}s; returns false otherwise
	 * 
	 * @param methodParameters
	 *            the array of {@link Parameter}s to evaluate the order and the
	 *            types of
	 * @return true if the array of {@link Parameter}s passed as parameter contains
	 *         parameters of the same type and in the same order than this current
	 *         {@link Set}; false otherwise
	 */
	public boolean validParameters(Object[] methodParameters) {
		int length = methodParameters == null ? 0 : methodParameters.length;

		if (this.parameters.length != length) {
			return false;
		}
		int index = 0;
		for (; index < length; index++) {
			this.parameters[index].reset();

			if (!this.parameters[index].validParameter(methodParameters[index])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the objects array passed as parameter contains valid object
	 * values to set of the same type and in the same order than this current
	 * {@link Set} ; returns false otherwise
	 * 
	 * @param methodParameters
	 *            the array of {@link Parameter}s to evaluate the order and the
	 *            types of
	 * @return true if the array of {@link MethodParameter}s passed as parameter
	 *         contains parameters of the same type and in the same order than this
	 *         current {@link Set}; false otherwise
	 */
	public boolean validParameters(JSONArray methodParameters) {
		int length = methodParameters == null ? 0 : methodParameters.length();

		if (this.parameters.length != length) {
			return false;
		}
		int index = 0;
		for (; index < length; index++) {
			this.parameters[index].reset();

			if (!this.parameters[index].validParameter(methodParameters.optJSONObject(index))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the length of the held array of parameters
	 * 
	 * @return the length of the held array of parameters
	 */
	public int length() {
		return this.parameters.length;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see JSONable#getJSON()
	 */
	@Override
	public String getJSON() {
		return getJSONObjectDescription().toString();
	}

	/**
	 * Returns the JSON object representation of the described {@link Signature}
	 * 
	 * @return the JSON object representation of the described {@link Signature}
	 */
	public JSONObject getJSONObjectDescription() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", this.getName());

		JSONArray paramtersArray = new JSONArray();

		Iterator<Parameter> iterator = this.iterator();

		while (iterator.hasNext()) {
			paramtersArray.put(((Parameter) iterator.next()).getJSONObject());
		}
		jsonObject.put("parameters", paramtersArray);
		return jsonObject;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Parameter> iterator() {
		return new ParameterIterator();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new Signature(this.mediator, this.name, this.returnedType, this.parameters);
	}

	private final class ParameterIterator implements Iterator<Parameter> {
		private int position = -1;
		private Parameter next = null;

		/**
		 * Constructor
		 */
		ParameterIterator() {
			findNext();
		}

		/**
		 * @inheritDoc
		 *
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return next != null;
		}

		/**
		 * @inheritDoc
		 *
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Parameter next() {
			Parameter parameter = next;
			findNext();
			return parameter;
		}

		/**
		 * @inheritDoc
		 *
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// not implemented
		}

		/**
		 * 
		 */
		private final void findNext() {
			position += 1;
			next = null;
			if (position < Signature.this.length()) {
				next = Signature.this.parameters[position];
			}
		}
	}
}

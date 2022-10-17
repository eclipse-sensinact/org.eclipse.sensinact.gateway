/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shortcut to a {@link Signature} of an {@link AccessMethod}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Shortcut extends Signature {	
	private static final Logger LOG=LoggerFactory.getLogger(Shortcut.class);
	private final Map<Integer, Parameter> fixedParameters;
	private final Stack<Shortcut> shortucts;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the Shortcut to be instantiated to interact
	 * with the OSGi host environment 
	 * @param type the type of the {@link AccessMethod} associates to the Shortcut to be
	 * instantiated
	 * @param parameterTypes the array of parameter types of the Shortcut to be
	 * instantiated
	 * @param parameterNames the array of parameter names of the Shortcut to be
	 * instantiated
	 * @param fixedParameters the set of fixed {@link Parameter}s mapped to their index in the
	 *  method signature
	 * @throws InvalidValueException
	 */
	public Shortcut(Mediator mediator, AccessMethod.Type type, Class<?>[] parameterTypes, String[] parameterNames,
			Map<Integer, Parameter> fixedParameters) throws InvalidValueException {
		super(mediator, type, parameterTypes, parameterNames);
		this.fixedParameters = Collections.unmodifiableMap(fixedParameters);
		this.shortucts = new Stack<Shortcut>();
	}
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the Shortcut to be instantiated to interact
	 * with the OSGi host environment 
	 * @param methodType the type of the associated {@link AccessMethod}
	 * @param responseType the object type returned by the associated {@link AccessMethod}
	 * @param parameters the {@link Parameter}s array parameterizing the call
	 * @param fixedParameters the set of fixed {@link Parameter}s mapped to their index in the
	 * method signature
	 * @throws InvalidValueException 
	 */
	public Shortcut(Mediator mediator, String methodType, Parameter[] parameters, Map<Integer, Parameter> fixedParameters) 
	throws InvalidValueException {
		super(mediator, methodType, parameters);
		this.fixedParameters = Collections.unmodifiableMap(fixedParameters);
		this.shortucts = new Stack<Shortcut>();
	}

	/**
	 * Constructor
	 * 
	 * 
	 * @param mediator the {@link Mediator} allowing the Shortcut to be instantiated to interact
	 * with the OSGi host environment 
	 * @param methodType the type of the associated {@link AccessMethod}
	 * @param responseType the object type returned by the associated {@link AccessMethod}
	 * @param parameters the {@link Parameter}s array parameterizing the call
	 * @param fixedParameters the set of fixed {@link Parameter}s mapped to their index in the
	 * method signature
	 * @param varArgs boolean defining whether thz Shortcut to be instantiated includes an optional
	 * variable Objects array argument
	 * @throws InvalidValueException 
	 */
	public Shortcut(Mediator mediator, String name, AccessMethodResponse.Response returnedType, Parameter[] parameters,
	Map<Integer, Parameter> fixedParameters) throws InvalidValueException {
		super(mediator, name, returnedType, parameters);
		this.fixedParameters = Collections.unmodifiableMap(fixedParameters);
		this.shortucts = new Stack<Shortcut>();
	}

	/**
	 * Pushes the Shortcut whose fixed parameters have to be used while building the
	 * object values array
	 * 
	 * @param shortcut the Shortcut to push
	 */
	public void push(Shortcut shortcut) {
		if (shortcut == null) 
			return;		
		this.shortucts.push(shortcut);
	}

	/**
	 * Returns the map of this Shortcut fixed {@link Parameter}s
	 * 
	 * @return the map of this Shortcut fixed {@link Parameter}s
	 */
	public Map<Integer, Parameter> getFixedParameters() {
		return this.fixedParameters;
	}

	@Override
	Object[] values(Iterator<Parameter> iterator) {
		int position = 0;
		Map<Integer, Parameter> gathered = new HashMap<Integer, Parameter>();
		while (!this.shortucts.isEmpty()) {
			gathered.putAll(this.shortucts.pop().getFixedParameters());
		}
		gathered.putAll(this.fixedParameters);
		Object[] values = new Object[super.length() + gathered.size()];

		Parameter parameter = null;
		for (; position < values.length; position++) {
			parameter = gathered.get(position);
			if (parameter == null && iterator.hasNext()) 
				parameter = iterator.next();			
			values[position] = parameter.getValue();
		}
		return values;
	}

	@Override
	public Object clone() {
		try {
			return new Shortcut(super.mediator, super.name, super.returnedType, super.parameters, this.fixedParameters);
		} catch (InvalidValueException e) {
			LOG.error(e.getMessage(),e);
		}
		return null;
	}
}

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
package org.eclipse.sensinact.gateway.core.method.trigger;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Extended {@link Executable} whose execution is parameterized
 * by an Object and returning an Object
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethodTrigger<P>
extends Executable<P,Object>, JSONable
{	
	/**
	 *  handled {@link AccessMethodTrigger} types
	 */
	public enum Type
	{
		//constant value associated to the one
		//of the linked parameter
		CONDITIONAL,
		// constant value
		CONSTANT,
		// copy one of the linked parameter
		COPY;
	}
	
	/**
	 * AccessMethodTrigger's parameters type
	 */
	public enum Parameters
	{
		//No Parameter Executor<Void,Object> 
		//expected
		EMPTY,
		//Initial Parameters array 
		//Executor<Object[],Object> expected
		PARAMETERS,
		//Intermediate AccessMethodResult Parameter 
		//Executor<AccessMethodResult,Object> expected
		INTERMEDIATE,
		//Resulting AccessMethodResponse Parameter
		//Executor<AccessMethodResponse,Object> expected
		RESPONSE;
	}
	
	public static final String TRIGGERS_ARRAY_KEY = "triggers";
	public static final String TRIGGER_KEY = "trigger";
	public static final String TRIGGER_CONSTANTS_KEY = "constants";
	public static final String TRIGGER_CONSTANT_KEY = "constant";
	public static final String TRIGGER_CONSTRAINT_KEY = "constraint";
	public static final String TRIGGER_TYPE_KEY = "type";
	public static final String TRIGGER_INDEX_KEY = "index";
	public static final String TRIGGER_PASS_ON = "passOn";
		
	/**
	 * Returns this AccessMethodTrigger's {@link Parameters} 
	 * enum instance 
	 * 
	 * @return
	 * 		this AccessMethodTrigger's {@link Parameters} 
	 * 		enum instance 
	 */
	Parameters getParameters();
	
	/**
	 * Returns this {@link AccessMethodTrigger}'s name
	 * 
	 * @return
	 * 		this {@link AccessMethodTrigger}'s name
	 */
	String getName();

	/**
	 * Returns true if the trigger execution result must
	 * be set to the remote counterpart to which the updated
	 * {@link StateVariableResource} is connected to ; returns
	 * false otherwise
	 *   
	 * @return
	 * 		<ul>
	 * 			<li>true if the remote connected counterpart	
	 * 				has to be updated;</li>
	 * 			<li>false otherwise</li>
	 * 		</ul>
	 */
    boolean passOn();
}

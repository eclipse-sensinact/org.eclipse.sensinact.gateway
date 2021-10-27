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
package org.eclipse.sensinact.gateway.core.method.trigger;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.core.StateVariableResource;

/**
 * Extended {@link Executable} whose execution is parameterized by an Object and
 * returning an Object
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethodTrigger extends Executable, JSONable {
	/**
	 * handled {@link AccessMethodTrigger} types
	 */
	public enum Type {
		// constant value associated to the one
		// of the trigger argument
		CONDITIONAL,
		// constant value
		CONSTANT,
		// copy one of the trigger argument
		COPY;
	}

	public static final String TRIGGERS_ARRAY_KEY = "triggers";
	public static final String TRIGGER_KEY = "trigger";
	public static final String TRIGGER_CONSTANTS_KEY = "constants";
	public static final String TRIGGER_CONSTANT_KEY = "constant";
	public static final String TRIGGER_CONSTRAINT_KEY = "constraint";
	public static final String TRIGGER_TYPE_KEY = "type";
	public static final String TRIGGER_BUILDER_KEY = "builder";
	public static final String TRIGGER_ARGUMENT_KEY = "argument";
	public static final String TRIGGER_PASSON_KEY = "passOn";
	
	/**
	 * @return the argument 
	 */
	<T> T getArgument();
	
	/**
	 * Returns this AccessMethodTrigger's {@link TriggerArgumentBuilder} name
	 * 
	 * @return this AccessMethodTrigger's {@link TriggerArgumentBuilder} name
	 */
	String getArgumentBuilder();

	/**
	 * Returns this {@link AccessMethodTrigger}'s name
	 * 
	 * @return this {@link AccessMethodTrigger}'s name
	 */
	String getName();

	/**
	 * Returns true if the trigger execution result must be set to the remote
	 * counterpart to which the updated {@link StateVariableResource} is connected
	 * to ; returns false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if the remote connected counterpart has to be updated;</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean passOn();
}

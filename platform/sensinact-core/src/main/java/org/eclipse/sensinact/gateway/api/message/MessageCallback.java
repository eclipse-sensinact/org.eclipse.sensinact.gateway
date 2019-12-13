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
package org.eclipse.sensinact.gateway.api.message;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * Callback service dedicated to {@link SnaMessage}s
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface MessageCallback extends Nameable {
	static final long ENDLESS = -1;

	enum Type {
		UNARY("unary"), 
		BUFFERIZED("buffer"), 
		SCHEDULED("scheduler"), 
		BUFFERERIZED_AND_SCHEDULED("scheduled-buffer");

		private final String operator;

		Type(String operator) {
			this.operator = operator;
		}

		public static Type fromOperator(String operator) {
			Type values[] = Type.values();
			int index = 0;
			int length = values.length;
			for (; index < length; index++) {
				if (values[index].getOperator().equals(operator)) {
					return values[index];
				}
			}
			return null;
		}

		private String getOperator() {
			return this.operator;
		}
	}

	/**
	 * Returns this MessageCallback's {@link ErrorHandler}, in charge of 
	 * defining the error treatment policy
	 * 
	 * @return this MessageCallback's {@link ErrorHandler}
	 */
	ErrorHandler getCallbackErrorHandler();

	/**
	 * Returns this callback's timeout
	 * 
	 * @return this callback's timeout
	 */
	long getTimeout();

	/**
	 * Returns the {@link MessageRegisterer}of this MessageCallback
	 * 
	 * @return this MessageCallback's {@link MessageRegisterer}
	 */
	MessageRegisterer getMessageRegisterer();
	
	/**
	 * Returns true if this MidCallback is active; otherwise
	 * returns false. The active status of this MidCallback depends 
	 * on the potential occurrence of message transmission errors 
	 * and of the error handling policy, on the callback life time,
	 * and on any 
	 *  
	 * @return
	 * 		<ul>
	 * 			<li>true if this MidCallback is active</li>
	 * 			<li>false otherwise</li>
	 * 		</ul>
	 */		
	boolean isActive();
}

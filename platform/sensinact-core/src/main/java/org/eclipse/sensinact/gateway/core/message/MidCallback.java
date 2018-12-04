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
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * {@link SnaMessage} callback service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MidCallback extends Nameable {
	static final long ENDLESS = -1;

	enum Type {
		UNARY("unary"), BUFFERIZED("buffer"), SCHEDULED("scheduler"), BUFFERERIZED_AND_SCHEDULED("scheduled-buffer");

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
	 * Returns this MidCallback's error handler. The {@link ErrorHandler} 
	 * is in charge of defining the error treatment policy for the
	 * {@link SnaMessageListener} holding it
	 * 
	 * @return this SnaCallback's error handler
	 */
	ErrorHandler getCallbackErrorHandler();

	/**
	 * Returns this callback's timeout
	 * 
	 * @return this callback's timeout
	 */
	long getTimeout();

	/**
	 * Returns the {@link MessageReisterer} associated to this MidCallback
	 * 
	 * @return the {@link MessageReisterer} of this SnaCallback
	 */
	MessageRegisterer getMessageRegisterer();
	
	/**
	 * Returns true if this MidCallback is active; otherwise
	 * returns false. The active status of this MidCallback depends 
	 * on the potential occurrence of message transmission errors 
	 * and of the error handling policy
	 *  
	 * @return
	 * 		<ul>
	 * 			<li>true if this MidCallback is active</li>
	 * 			<li>false otherwise</li>
	 * 		</ul>
	 */		
	boolean isActive();
}

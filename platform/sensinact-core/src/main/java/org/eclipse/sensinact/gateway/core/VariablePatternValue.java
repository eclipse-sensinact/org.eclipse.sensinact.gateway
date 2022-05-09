/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

/**
 *
 */
public interface VariablePatternValue {
	/**
	 * Builds and returns the next value of the variable part of the {@link StringPatternValue} to 
	 * which this VariablePatternValue is attached to
	 * 
	 * @return the next value of the variable part of the associated {@link StringPatternValue}
	 */
	String next();

	/**
	 * Returns the current value of the variable part of the {@link StringPatternValue} to which 
	 * this VariablePatternValue is attached to
	 * 
	 * @return the current value of the variable part of the associated {@link StringPatternValue}
	 */
	String get();
	
	/**
	 * Resets (if relevant) the value of the variable part of the {@link StringPatternValue} to 
	 * which this VariablePatternValue is attached to
	 */
	void reset();
}

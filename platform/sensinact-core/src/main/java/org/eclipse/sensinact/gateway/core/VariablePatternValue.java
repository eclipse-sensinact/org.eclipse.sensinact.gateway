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

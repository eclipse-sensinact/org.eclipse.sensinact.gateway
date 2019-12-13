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
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Extended {@link Executable} whose execution is parameterized by an Object and
 * returning an Object
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface DynamicParameterValue extends JSONable {
	/**
	 * handled {@link DynamicParameterValue} types
	 */
	public enum Type {
		// constant value associated to the one
		// of the linked parameter
		CONDITIONAL,
		// copy one of the linked parameter
		COPY;
	}

	public static final String BUILDERS_ARRAY_KEY = "builers";
	public static final String BUILDER_KEY = "builder";
	public static final String BUILDER_CONSTANTS_KEY = "constants";
	public static final String BUILDER_CONSTANT_KEY = "constant";
	public static final String BUILDER_CONSTRAINT_KEY = "constraint";
	public static final String BUILDER_RESOURCE_KEY = "resource";
	public static final String BUILDER_TYPE_KEY = "type";
	public static final String BUILDER_PARAMETER_KEY = "parameter";

	/**
	 * Returns this DynamicParameterValue's name
	 * 
	 * @return this DynamicParameterValue's name
	 */
	String getName();

	/**
	 * Returns this DynamicParameterValue's value
	 * 
	 * @return this DynamicParameterValue's value
	 */
	Object getValue();

	/**
	 * Returns the name of the resource to which this DynamicParameterValue is
	 * linked to
	 * 
	 * @return the name of the linked resource
	 */
	String getResource();

	/**
	 * Defines the {@link Executable} value extractor of the associated
	 * {@link ResourceImpl}
	 * 
	 * @param resourceValueExtractor
	 *            the {@link Executable} value extractor to set
	 */
	void setResourceValueExtractor(Executable<Void, Object> extractor);

}

/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method.trigger;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Extended {@link AccessMethodTrigger} returning the value of the Primitive
 * parameter when executed
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Copy extends AbstractAccessMethodTrigger {
	public static final String NAME = "COPY";


	/**
	 * Constructor
	 * 
	 * @param index
	 *            the index of the execution parameter to copy
	 */
	public Copy(Object argument, String argumentBuilder, boolean passOn) {
		super(argument, argumentBuilder, passOn);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see Executable#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object parameter) throws Exception {
		return parameter;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see JSONable#getJSON()
	 */
	@Override
	public String doGetJSON() {
		return null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see AccessMethodTrigger#getName()
	 */
	@Override
	public String getName() {
		return Copy.NAME;
	}
}

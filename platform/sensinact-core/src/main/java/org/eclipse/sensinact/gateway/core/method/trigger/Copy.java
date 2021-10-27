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

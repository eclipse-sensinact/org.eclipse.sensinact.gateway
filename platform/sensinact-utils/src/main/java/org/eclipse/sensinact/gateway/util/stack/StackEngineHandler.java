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

package org.eclipse.sensinact.gateway.util.stack;

/**
 * Service connected to a {@link StackEngine}, in charge
 * of handling popped off elements
 * 
 * @param <E>
 * 		the type of handled element
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface StackEngineHandler<E>
{
	/**
	 * Handles the element popped off from the connected
	 * {@link StackEngine}
	 * 
	 * @param element
	 * 		the popped off <code>&lt;E&gt;</code> element
	 * 		to handle
	 */
	void doHandle(E element);
}

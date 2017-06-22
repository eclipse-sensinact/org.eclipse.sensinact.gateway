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
package org.eclipse.sensinact.gateway.common.automata;

/**
 * Signature of a service allowing to create {@link Frame}
 * implementation instances 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface FrameFactory 
{
	/**
	 * Returns a new empty {@link Frame} implementation
	 * instance according to the {@link FrameModel} passed
	 * as parameter
	 * 
	 * @param model
	 * 		the {@link FrameModel} description of the 
	 * 		{@link Frame} to build
	 * 
	 * @return
	 * 		a new empty {@link Frame} implementation
	 * 		instance
	 * 
	 * @throws FrameException
	 */
	Frame newInstance(FrameModel model) throws FrameException;
}

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
 * Exception thrown when a specified implementation class of a frame type
 * cannot be load
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FrameModelClassNotFoundException extends FrameModelException
{
	/**
	 * Constructor
	 * 
	 * @param message
	 * 		the error message
	 */
	public FrameModelClassNotFoundException(String message)
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 * 		the error message
	 * @param throwable
	 * 		wrapped exception that has made the current one thrown
	 */
	public FrameModelClassNotFoundException(String message,Throwable throwable)
	{
		super(message,throwable);
	}
	
	/**
	 * Constructor
	 * 
	 * @param throwable
	 * 		wrapped exception that has made the current one thrown
	 */
	public FrameModelClassNotFoundException(Throwable throwable)
	{
		super(throwable);
	}
}

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
 * Exception thrown when the frame schema cannot be found
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FrameSchemaNotFoundException extends FrameModelException
{
	/**
	 * Constructor
	 * 
	 * @param message
	 * 		the error message
	 */
	public FrameSchemaNotFoundException(String message)
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
	public FrameSchemaNotFoundException(String message,Throwable throwable)
	{
		super(message,throwable);
	}
	
	/**
	 * Constructor
	 * 
	 * @param throwable
	 * 		wrapped exception that has made the current one thrown
	 */
	public FrameSchemaNotFoundException(Throwable throwable)
	{
		super(throwable);
	}
}

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
package org.eclipse.sensinact.gateway.security.signature.exception;

public class BundleValidationException extends Exception 
{
	/**
	 * Constructor
	 * 
	 * @param message the message of the hire exception
	 */
	public BundleValidationException(String message)
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param e the cause Exception of this one 
	 */
	public BundleValidationException(Exception e)
	{
		super(e);
	}

	public static final long serialVersionUID = 110;
}

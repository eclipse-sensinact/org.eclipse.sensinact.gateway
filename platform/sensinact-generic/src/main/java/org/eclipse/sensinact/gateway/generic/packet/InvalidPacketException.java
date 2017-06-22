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
package org.eclipse.sensinact.gateway.generic.packet;

/**
 *	Exception thrown when an invalid communication {@link Packet} 
 *	object is detected 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class InvalidPacketException extends Exception {

	/**
	 * Generated unique serial identifier
	 */
	private static final long serialVersionUID = -3016263812460929749L;

	/**
	 * Constructor
	 */
	public InvalidPacketException()
	{
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 * 		the exception message
	 */
	public InvalidPacketException(String message)
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param cause
	 * 		the {@link Throwable} object which has caused the
	 * 		current exception
	 */
	public InvalidPacketException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 * 		the exception message
	 * @param cause
	 * 		the {@link Throwable} object which has caused the
	 * 		current exception
	 */
	public InvalidPacketException(String message, Throwable cause) 
	{
		super(message, cause);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 * 		the exception message
	 * @param cause
	 * 		the {@link Throwable} object which has caused the
	 * 		current exception
	 * @param enableSuppression
	 * 		defines whether the suppression is enabled or not
	 * @param writableStackTrace
	 * 		defines whether the current exception's stack trace 
	 * 		is writable or not
	 */
	public InvalidPacketException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) 
	{
        super(message, cause/*, enableSuppression , writableStackTrace*/);
	}

}

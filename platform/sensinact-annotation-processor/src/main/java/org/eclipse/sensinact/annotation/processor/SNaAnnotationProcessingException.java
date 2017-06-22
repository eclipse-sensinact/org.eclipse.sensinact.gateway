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

package org.eclipse.sensinact.annotation.processor;

/**
 * Thrown when a constraint applying on a sensiNact 
 * annotation is not satisfied
 */
@SuppressWarnings("serial")
public class SNaAnnotationProcessingException 
extends Exception
{   
    /**
     * Constructor
     */
    public SNaAnnotationProcessingException() 
    {
        super();
    }

    /**
     * Constructor 
     * 
     * @param message
     *      the error message
     */
    public SNaAnnotationProcessingException(String message)
    {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param cause
     *      the Throwable object which has caused the triggering 
     *      of this exception
     */
    public SNaAnnotationProcessingException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor
     * 
     * @param message
     *      the error message
     * @param cause
     *      the Throwable object which has caused the triggering 
     *      of this exception
     */
    public SNaAnnotationProcessingException(String message, 
            Throwable cause)
    {
        super(message, cause);
    }
}

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
package org.eclipse.sensinact.gateway.generic;

/**
 * Exception thrown by a resource at initialization time if an error
 * occurred
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class NoAnnotationFoundException extends Exception {
    /**
     * Constructor
     */
    public NoAnnotationFoundException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public NoAnnotationFoundException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the {@link Throwable} object which has caused the current
     *              exception
     */
    public NoAnnotationFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the exception message
     * @param cause   the {@link Throwable} object which has caused the current
     *                exception
     */
    public NoAnnotationFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

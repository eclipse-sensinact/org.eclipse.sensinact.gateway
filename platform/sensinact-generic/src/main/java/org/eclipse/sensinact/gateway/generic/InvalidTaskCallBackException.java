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
package org.eclipse.sensinact.gateway.generic;

/**
 * Exception thrown by a resource at initialization time if an error
 * occurred
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class InvalidTaskCallBackException extends Exception {
    /**
     * Constructor
     */
    public InvalidTaskCallBackException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public InvalidTaskCallBackException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the {@link Throwable} object which has caused the current
     *              exception
     */
    public InvalidTaskCallBackException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the exception message
     * @param cause   the {@link Throwable} object which has caused the current
     *                exception
     */
    public InvalidTaskCallBackException(String message, Throwable cause) {
        super(message, cause);
    }
}

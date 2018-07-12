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
package org.eclipse.sensinact.gateway.core;

/**
 * Thrown to indicate a resource related exception.
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceException extends RuntimeException {
    /**
     * Constructs an <code>ResourceException</code> with no detail message.
     */
    public ResourceException() {
        super();
    }

    /**
     * Constructs an <code>ResourceException</code> with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public ResourceException(String message) {
        super(message);
    }

    /**
     * Constructs an <code>ResourceException</code> with the specified detail
     * message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an <code>ResourceException</code> with the specified cause.
     *
     * @param cause the cause
     */
    public ResourceException(Throwable cause) {
        super(cause);
    }
}

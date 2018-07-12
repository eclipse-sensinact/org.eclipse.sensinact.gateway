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
package org.eclipse.sensinact.gateway.common.primitive;

/**
 * Exception thrown if an error occurred while setting the value of a
 * {@link Primitive}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public class InvalidValueException extends Exception {
    /**
     * Constructor
     */
    public InvalidValueException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the error message
     */
    public InvalidValueException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the Throwable object which has caused the triggering of this
     *              exception
     */
    public InvalidValueException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the error message
     * @param cause   the Throwable object which has caused the triggering of this
     *                exception
     */
    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}

/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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

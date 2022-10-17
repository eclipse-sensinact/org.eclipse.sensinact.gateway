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
 * Exception thrown if an error occurred if the type of the object set as
 * value of a {@link Primitive} is invalid
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public class InvalidValueTypeException extends InvalidValueException {
    /**
     * Constructor
     */
    public InvalidValueTypeException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the error message
     */
    public InvalidValueTypeException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the Throwable object which has caused the triggering of this
     *              exception
     */
    public InvalidValueTypeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the error message
     * @param cause   the Throwable object which has caused the triggering of this
     *                exception
     */
    public InvalidValueTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}

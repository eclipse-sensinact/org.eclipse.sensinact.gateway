/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

/**
 * Exception thrown when an invalid communication {@link Packet}
 * object is detected
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class InvalidProtocolStackException extends Exception {
    /**
     * Generated unique serial identifier
     */
    private static final long serialVersionUID = -3016263812460929749L;

    /**
     * Constructor
     */
    public InvalidProtocolStackException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public InvalidProtocolStackException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the {@link Throwable} object which has caused the
     *              current exception
     */
    public InvalidProtocolStackException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the exception message
     * @param cause   the {@link Throwable} object which has caused the
     *                current exception
     */
    public InvalidProtocolStackException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param message            the exception message
     * @param cause              the {@link Throwable} object which has caused the
     *                           current exception
     * @param enableSuppression  defines whether the suppression is enabled or not
     * @param writableStackTrace defines whether the current exception's stack trace
     *                           is writable or not
     */
    public InvalidProtocolStackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause/*, enableSuppression , writableStackTrace*/);
    }
}

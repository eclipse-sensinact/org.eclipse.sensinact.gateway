/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.parser;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class InvalidXmlDefinitionException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public InvalidXmlDefinitionException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public InvalidXmlDefinitionException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the {@link Throwable} object which has caused the current
     *              exception
     */
    public InvalidXmlDefinitionException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the exception message
     * @param cause   the {@link Throwable} object which has caused the current
     *                exception
     */
    public InvalidXmlDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}

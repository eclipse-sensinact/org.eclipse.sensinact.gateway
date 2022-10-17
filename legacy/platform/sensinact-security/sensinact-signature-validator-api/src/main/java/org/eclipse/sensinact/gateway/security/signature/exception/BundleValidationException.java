/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.signature.exception;

public class BundleValidationException extends Exception {
    /**
     * Constructor
     *
     * @param message the message of the hire exception
     */
    public BundleValidationException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param e the cause Exception of this one
     */
    public BundleValidationException(Exception e) {
        super(e);
    }

    public static final long serialVersionUID = 110;
}

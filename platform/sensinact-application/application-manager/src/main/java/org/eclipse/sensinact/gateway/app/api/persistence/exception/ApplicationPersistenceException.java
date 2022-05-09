/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.persistence.exception;

public class ApplicationPersistenceException extends Exception {
    public ApplicationPersistenceException() {
        super();
    }

    public ApplicationPersistenceException(String message) {
        super(message);
    }

    public ApplicationPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationPersistenceException(Throwable cause) {
        super(cause);
    }

    protected ApplicationPersistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

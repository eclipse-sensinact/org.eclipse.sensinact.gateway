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

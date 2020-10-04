/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.util.rest;

/**
 * Thrown by {@link RestLikeMapper}
 */
public class RestLikeMapperException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public RestLikeMapperException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message the exception message
     */
    public RestLikeMapperException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause the {@link Throwable} object which has caused the
     *              current exception
     */
    public RestLikeMapperException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message the exception message
     * @param cause   the {@link Throwable} object which has caused the
     *                current exception
     */
    public RestLikeMapperException(String message, Throwable cause) {
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
    public RestLikeMapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause/*, enableSuppression, writableStackTrace*/);
    }
}

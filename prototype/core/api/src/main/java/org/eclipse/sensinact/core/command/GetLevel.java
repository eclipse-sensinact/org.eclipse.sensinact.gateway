/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.command;

/**
 * Variants of the GET command
 */
public enum GetLevel {

    /**
     * Default level. For pull-based values, if the cached value is older than the
     * threshold, pull the real value, else return the cached one. For push-based
     * values, returns the last cached value.
     */
    CACHED,

    /**
     * Weak Get: always return the last value in cache. Don't try to pull a real
     * value
     */
    WEAK,

    /**
     * Hard Get: always ask for the real value and put it in cache. Acts like
     * {@link #CACHED} for pushed values.
     */
    HARD,
}

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
 * Levels of execution of the GET command.
 * <p>
 * These levels only have effect on resources with an external getter. GET
 * commands execution upon other resources will always return the cached value.
 */
public enum GetLevel {

    /**
     * Default level for the GET command
     * <p>
     * For resources with an external getter, the cached value will be returned if
     * its time stamp is in the cache period. If the value wasn't set or if the
     * cache expired, the external getter will be called.
     */
    NORMAL,

    /**
     * Weak Get
     * <p>
     * Always returns the cached value, never calls the external getter (if defined)
     * even if the resource has not yet been set.
     */
    WEAK,

    /**
     * Strong Get
     * <p>
     * Always calls the external getter of the resource, if defined.
     */
    STRONG,
}

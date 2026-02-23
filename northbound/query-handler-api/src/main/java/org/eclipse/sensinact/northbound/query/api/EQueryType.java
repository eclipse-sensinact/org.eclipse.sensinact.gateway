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
package org.eclipse.sensinact.northbound.query.api;

import org.eclipse.sensinact.northbound.query.dto.SensinactPath;

/**
 * Operation to apply on a {@link SensinactPath}
 */
public enum EQueryType {
    /**
     * List items
     */
    LIST,

    /**
     * Describe item behind path
     */
    DESCRIBE,

    /**
     * Get a resource value (path must describe a unique resource)
     */
    GET,

    /**
     * Sets a resource value (path must describe a unique resource)
     */
    SET,

    /**
     * Get a snapshot
     */
    GET_SNAPSHOT,

    /**
     * Modify a provider link
     */
    LINK,

    /**
     * Acts on a resource (path must describe a unique resource)
     */
    ACT,

    /**
     * <code>Reserved:</code> TODO
     */
    SUBSCRIBE,

    /**
     * <code>Reserved:</code> TODO
     */
    UNSUBSCRIBE,
}

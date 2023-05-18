/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.annotation.dto;

/**
 * This enum defines the rules for handling values provided in a map
 */
public enum MapAction {
    /**
     * Treat an annotated Map field as a collection of multiple name/values rather
     * than a single value
     */
    USE_KEYS_AS_FIELDS,
    /**
     * If a key maps to null then remove that value
     */
    REMOVE_NULL_VALUES,
    /**
     * If a key is not present then remove that value
     */
    REMOVE_MISSING_VALUES
}

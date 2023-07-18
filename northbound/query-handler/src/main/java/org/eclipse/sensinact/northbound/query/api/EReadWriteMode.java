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
package org.eclipse.sensinact.northbound.query.api;

import org.eclipse.sensinact.core.model.ValueType;

/**
 * External read/write mode
 */
public enum EReadWriteMode {
    /** Read only */
    RO,

    /** Read / Write */
    RW;

    /**
     * Returns the read/write mode according to the resource value type
     *
     * @param type Resource value type
     * @return Read/Write if resource is Modifiable, else Read-Only
     */
    public static EReadWriteMode fromValueType(final ValueType type) {
        switch (type) {
        case MODIFIABLE:
            return RW;

        case FIXED:
        case UPDATABLE:
        default:
            return RO;

        }
    }
}

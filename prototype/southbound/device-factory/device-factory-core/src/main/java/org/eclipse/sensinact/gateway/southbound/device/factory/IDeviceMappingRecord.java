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
package org.eclipse.sensinact.gateway.southbound.device.factory;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;

/**
 * Represents an entry in a parsed device mapping input
 */
public interface IDeviceMappingRecord {

    /**
     * Returns the field value, converted in the expected type
     *
     * @param field   Field path
     * @param options Mapping options
     * @return The field value, in the expected type
     */
    Object getField(final RecordPath field, final DeviceMappingOptionsDTO options);

    /**
     * Returns the field value as a string
     *
     * @param field   Field path
     * @param options Mapping options
     * @return The field value as a string
     */
    String getFieldString(final RecordPath field, final DeviceMappingOptionsDTO options);
}

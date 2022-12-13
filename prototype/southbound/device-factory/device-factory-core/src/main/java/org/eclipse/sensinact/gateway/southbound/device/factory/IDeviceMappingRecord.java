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
package org.eclipse.sensinact.gateway.southbound.device.factory;

/**
 * Represents an entry in a parsed device mapping input
 */
public interface IDeviceMappingRecord {

    Object getField(RecordPath field);

    String getFieldString(RecordPath field);

    Integer getFieldInt(RecordPath field);
}

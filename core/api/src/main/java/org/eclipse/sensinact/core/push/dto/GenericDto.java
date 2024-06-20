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
package org.eclipse.sensinact.core.push.dto;

import java.time.Instant;

import org.eclipse.sensinact.core.annotation.dto.NullAction;

/**
 * A special update dto type where the data is found in "value" with an optional
 * target data type
 *
 * Used to define a schema for generic device access with no model (e.g. driven
 * by configuration)
 */
public final class GenericDto extends BaseValueDto {

    public Class<?> type;

    public Object value;

    /**
     * The timestamp for the data. If null then Instant.now will be used.
     */
    public Instant timestamp;

    /**
     * Action to apply if value is null
     */
    public NullAction nullAction = NullAction.IGNORE;
}

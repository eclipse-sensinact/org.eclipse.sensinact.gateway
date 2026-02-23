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

import org.eclipse.sensinact.core.annotation.dto.AnnotationConstants;
import org.eclipse.sensinact.core.annotation.dto.DuplicateAction;
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
     * The upper bound for the resource data. If not set then the resource bounds
     * will be set based on the type of {@link #value}. Specifically:
     *
     * <ul>
     *   <li> An array type = -1</li>
     *   <li> <code>List</code>, <code>Set</code>, <code>Collection</code> = -1</li>
     *   <li> All other types = 1</li>
     * <ul>
     *
     * A bound of <code>-1</code> means no upper limit. A bound of <code>1</code> means
     * that the resource value is unary.
     * @return
     */
    public int upperBound = AnnotationConstants.NO_UPPER_BOUND_SET;

    /**
     * The timestamp for the data. If null then Instant.now will be used.
     */
    public Instant timestamp;

    /**
     * Action to apply if value is null
     */
    public NullAction nullAction = NullAction.IGNORE;

    /**
     * Action to apply if value is the same as the current resource value
     */
    public DuplicateAction duplicateDataAction = DuplicateAction.UPDATE_ALWAYS;

    /**
     * Action to apply if a metadata value is the same as the current metadata value
     */
    public DuplicateAction duplicateMetadataAction = DuplicateAction.UPDATE_IF_DIFFERENT;
}

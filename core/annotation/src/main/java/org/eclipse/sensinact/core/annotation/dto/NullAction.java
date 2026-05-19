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
 * Defines the action to take if a value is null
 */
public enum NullAction {
    /**
     * If the data field is null then ignore it and do not update the value
     */
    IGNORE,
    /**
     * If the data field is null then set null as the value
     */
    UPDATE,
    /**
     * If the data field is null then:
     * <ul>
     *   <li>If the resource has previously been set then set it to <code>null</code></li>
     *   <li>If the resource has never been set then ignore this field and do not update the value</li>
     * </ul>
     */
    UPDATE_IF_PRESENT,

    /**
     * If the data field is null and the action is applied on a resource, then
     * remove the overlay metadata value to let the model-level metadata value take
     * precedence. If the action is applied on a service or provider, then remove
     * the overlay metadata value to let the model-level metadata value take
     * precedence and remove all the overlay metadata values of the resources of the
     * service or provider to let the model-level metadata values be accessible.
     */
    REMOVE_OVERLAY,
}

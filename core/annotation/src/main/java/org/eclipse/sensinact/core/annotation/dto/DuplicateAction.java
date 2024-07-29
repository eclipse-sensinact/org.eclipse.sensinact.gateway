/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
 * This enum defines the rules for handling duplicate values
 */
public enum DuplicateAction {
    /**
     * Update the value (or metadata value) even if the new value
     * is the same as the old value. This will cause the timestamp
     * associated with the value to be updated.
     */
    UPDATE_ALWAYS,
    /**
     * Update the value (or metadata value) only if the new value
     * is different from the old value, determined by equality.
     * If the new value is the same as the old value then no
     * update will be made and no timestamp change will occur.
     */
    UPDATE_IF_DIFFERENT
}

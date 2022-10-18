/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.generic.dto;

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

}
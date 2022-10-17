/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.annotation.dto;

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
	UPDATE
}
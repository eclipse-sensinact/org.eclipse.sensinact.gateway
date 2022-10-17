/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.model;

public enum ResourceType {
	/**
	 * An action resource can be acted upon to actuate the device
	 * and get a result 
	 */
	ACTION, 
	/**
	 * A property resource can use GET and SET to read/write its value
	 */
	PROPERTY, 
	/**
	 * A sensor resource can use GET to see the latest value from the sensor
	 */
	SENSOR, 
	/**
	 * A state variable can use GET and will change as a result of an action
	 * or a change in the real world.
	 */
	STATE_VARIABLE;
}

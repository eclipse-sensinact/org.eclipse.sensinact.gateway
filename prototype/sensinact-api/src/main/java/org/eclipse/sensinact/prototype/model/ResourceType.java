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

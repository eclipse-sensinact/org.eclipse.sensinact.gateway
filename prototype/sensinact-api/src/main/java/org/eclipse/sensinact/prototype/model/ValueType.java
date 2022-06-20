package org.eclipse.sensinact.prototype.model;

/**
 * The type of the value
 */
public enum ValueType {

	/**
	 * The value can be modified by a SET operation
	 */
	MODIFIABLE, 
	/**
	 * This value may change over time, but not SET
	 */
	UPDATABLE, 
	/**
	 * This value cannot be SET, and will not change over time
	 */
	FIXED;
}

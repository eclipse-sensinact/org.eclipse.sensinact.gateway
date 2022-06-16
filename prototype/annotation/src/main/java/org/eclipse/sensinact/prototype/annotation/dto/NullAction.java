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
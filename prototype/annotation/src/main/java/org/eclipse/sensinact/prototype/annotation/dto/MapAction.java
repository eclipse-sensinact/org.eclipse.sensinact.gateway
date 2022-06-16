package org.eclipse.sensinact.prototype.annotation.dto;

/**
 * This enum defines the rules for handling values provided in a map
 */
public enum MapAction { 
	/**
	 * Treat an annotated Map field as a collection of multiple name/values rather than a single value
	 */
	USE_KEYS_AS_FIELDS,
	/**
	 * If a key maps to null then remove that value
	 */
	REMOVE_NULL_VALUES, 
	/**
	 * If a key is not present then remove that value
	 */
	REMOVE_MISSING_VALUES
}
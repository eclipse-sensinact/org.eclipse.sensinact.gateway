package org.eclipse.sensinact.prototype.generic.dto;

/**
 * A special update dto type where the data is found in "value" with an optional target data type
 *
 * Used to define a schema for generic device access with no model (e.g. driven by configuration)
 */
public class GenericDto extends BaseValueDto {
	
	public Class<?> type;
	
	public Object value;

}

package org.eclipse.sensinact.prototype.generic.dto;

import java.util.List;

/**
 * A special update dto type where multiple values are updated in a single event
 */
public final class BulkGenericDto {
	
	public List<GenericDto> dtos;

}

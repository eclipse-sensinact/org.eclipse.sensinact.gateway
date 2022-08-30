package org.eclipse.sensinact.prototype.dto.impl;

import java.util.Map;

public class MetadataUpdateDto extends AbstractUpdateDto {
	
	public boolean removeNullValues;
	
	public boolean removeMissingValues;
	
	public Map<String, Object> metadata;
}

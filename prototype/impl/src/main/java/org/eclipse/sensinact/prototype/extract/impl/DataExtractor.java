package org.eclipse.sensinact.prototype.extract.impl;

import java.util.List;

import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;

public interface DataExtractor {
	
	public List<? extends AbstractUpdateDto> getUpdates(Object update);

}

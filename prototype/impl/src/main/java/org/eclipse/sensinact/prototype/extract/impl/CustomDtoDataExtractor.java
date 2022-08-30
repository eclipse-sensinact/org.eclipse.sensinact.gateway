package org.eclipse.sensinact.prototype.extract.impl;

import java.util.List;
import java.util.function.Function;

import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;

public class CustomDtoDataExtractor implements DataExtractor {
	
	private final Function<Object, List<? extends AbstractUpdateDto>> mapper;
	
	public CustomDtoDataExtractor(Class<?> clazz) {
		mapper = AnnotationMapping.getUpdateDtoMappings(clazz);
	}

	@Override
	public List<? extends AbstractUpdateDto> getUpdates(Object update) {
		return mapper.apply(update);
	}
}

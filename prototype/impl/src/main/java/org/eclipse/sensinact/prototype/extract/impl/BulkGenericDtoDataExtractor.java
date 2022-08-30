package org.eclipse.sensinact.prototype.extract.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.generic.dto.BulkGenericDto;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;

public class BulkGenericDtoDataExtractor implements DataExtractor {

	private final GenericDtoDataExtractor subMapper = new GenericDtoDataExtractor();
	
	@Override
	public List<? extends AbstractUpdateDto> getUpdates(Object update) {
		
		BulkGenericDto dto = checkCast(update);
		
		List<GenericDto> list = dto.dtos == null ? Collections.emptyList() : dto.dtos;
		
		return list.stream()
			.map(subMapper::getUpdates)
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	private BulkGenericDto checkCast(Object update) {
		try {
			return BulkGenericDto.class.cast(update);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The supplied update dto is not of the correct type to extract", e);
		}
	}
}

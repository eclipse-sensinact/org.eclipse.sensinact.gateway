package org.eclipse.sensinact.prototype.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.command.IndependentCommands;
import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;
import org.eclipse.sensinact.prototype.extract.impl.BulkGenericDtoDataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.CustomDtoDataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.DataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.GenericDtoDataExtractor;
import org.eclipse.sensinact.prototype.generic.dto.BulkGenericDto;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;

@Component
public class PrototypePushImpl implements PrototypePush {
	//TODO wrap this in a more pleasant type?
	@Reference
	GatewayThread thread;
	
	/**
	 * We use a weak map so we don't keep classloaders for old bundles
	 */
	private final Map<Class<?>, DataExtractor> cachedExtractors = new WeakHashMap<>();

	@Override
	public Promise<?> pushUpdate(Object o) {
		
		DataExtractor extractor;
		
		Class<?> updateClazz = o.getClass();
		
		synchronized (cachedExtractors) {
			extractor = cachedExtractors.computeIfAbsent(updateClazz, this::createDataExtractor);
		}
		
		List<? extends AbstractUpdateDto> updates = extractor.getUpdates(o);
		
		return thread.execute(new IndependentCommands<>(
				updates.stream()
					.map(this::toCommand)
					.collect(toList()
			)
		));
	}
	
	private DataExtractor createDataExtractor(Class<?> clazz) {
		if(clazz == GenericDto.class) {
			return new GenericDtoDataExtractor();
		} else if (clazz == BulkGenericDto.class) {
			return new BulkGenericDtoDataExtractor();
		} else {
			return new CustomDtoDataExtractor(clazz);
		}
	}

	private AbstractSensinactCommand<Void> toCommand(AbstractUpdateDto dto) {
		if(dto instanceof DataUpdateDto) {
			return new SetValueCommand((DataUpdateDto) dto);
		} else if (dto instanceof MetadataUpdateDto) {
			return new SetMetadataCommand((MetadataUpdateDto) dto);
		} else {
			throw new IllegalArgumentException("Unknown dto type " + dto.getClass().toString());
		}
	}
	
}

package org.eclipse.sensinact.prototype.impl;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.extract.impl.BulkGenericDtoDataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.CustomDtoDataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.DataExtractor;
import org.eclipse.sensinact.prototype.extract.impl.GenericDtoDataExtractor;
import org.eclipse.sensinact.prototype.generic.dto.BulkGenericDto;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.osgi.service.component.annotations.Component;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

@Component
public class PrototypePushImpl implements PrototypePush {
	
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
		
		//TODO submit updates as an atomic batch, then trigger notifications
		
		return Promises.failed(new UnsupportedOperationException());
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
	
}

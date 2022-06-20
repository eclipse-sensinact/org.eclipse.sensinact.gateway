package org.eclipse.sensinact.prototype.generic;

import org.eclipse.sensinact.prototype.EventTopicNames;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventBus;

@Component
public class GenericPushComponent {
	
	@Reference
	private TypedEventBus bus;
	
	@Reference
	private PrototypePush sensiNact;
	
	/**
	 * Message coming in from the sensor, just like a custom model
	 */
	public void onMessage(String message) {
		// Create the DTO
		GenericDto dto = toDTO(message);

		// Send the dto data to sensiNact core somehow?
		
		// e.g. Typed Events
		bus.deliver(EventTopicNames.GENERIC_UPDATE_EVENTS, dto);
		
		// e.g. Direct to core
		sensiNact.pushUpdate(dto);
	}
	
	GenericDto toDTO(String message) {
		GenericDto dto = new GenericDto();
		// Populate the DTO
		return dto;
	}

}

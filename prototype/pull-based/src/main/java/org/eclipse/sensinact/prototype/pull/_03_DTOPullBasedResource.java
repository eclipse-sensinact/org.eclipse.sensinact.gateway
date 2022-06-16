package org.eclipse.sensinact.prototype.pull;

import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.Service;
import org.eclipse.sensinact.prototype.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.GET;
import org.eclipse.sensinact.prototype.annotation.verb.GET.ReturnType;
import org.osgi.service.component.annotations.Component;

/**
 * A DTO defines the resource(s) returned, but the provider is determined by the service properties
 */
@WhiteboardResource
@ProviderName("pull_based")
@Component(service = _03_DTOPullBasedResource.class)
public class _03_DTOPullBasedResource {

	@GET(ReturnType.DTO)
	public SimpleDTO getValue() {
		// Get the value from the sensor
		return null;
	}
	
	@Service("example")
	public static class SimpleDTO {
		
		@Data
		public int count;
		
		@Data
		public double average;

	}
}

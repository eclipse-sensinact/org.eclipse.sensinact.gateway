package org.eclipse.sensinact.prototype.writable;

import org.eclipse.sensinact.prototype.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.SET;
import org.osgi.service.component.annotations.Component;

/**
 * Service properties define the provider that this resource is for
 */
@WhiteboardResource
@ProviderName("pull_based")
@Component(service = _01_SimpleWritableBasedResource.class)
public class _01_SimpleWritableBasedResource {
	
	/**
	 * A GET method for a service and resource
	 * @return
	 */
	@SET(service = "example", resource = "default")
	public void setValue(Double d) {
		// Set the value for the device
	}
}

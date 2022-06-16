package org.eclipse.sensinact.prototype.writable;

import org.eclipse.sensinact.prototype.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.SET;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * Multiple providers from a single service, a single method for all resources
 */
@WhiteboardResource
@ProviderName({"foo", "bar", "foobar"})
@Component(service = _03_MultiWritableBasedResource.class)
public class _03_MultiWritableBasedResource {

	@SET(service = "example", resource = "fizz", type = String.class)
	@SET(service = "example", resource = "buzz", type = Long.class)
	@SET(service = "example2", resource = "fizzbuzz", type = Double.class)
	public void setValue(@UriParam(UriSegment.PROVIDER) String provider,
			@UriParam(UriSegment.SERVICE) String service, @UriParam(UriSegment.RESOURCE) String resource,
			Object value) {
		// Get the actual value from the sensor
	}
}

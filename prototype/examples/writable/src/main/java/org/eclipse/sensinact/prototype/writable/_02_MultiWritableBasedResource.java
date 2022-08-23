package org.eclipse.sensinact.prototype.writable;

import org.eclipse.sensinact.prototype.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.SET;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * Multiple providers from a single service, different methods for each resource
 */
@WhiteboardResource
@ProviderName({"foo", "bar", "foobar"})
@Component(service = _02_MultiWritableBasedResource.class)
public class _02_MultiWritableBasedResource {

	@SET(service = "example", resource = "fizz")
	public void setFizz(@UriParam(UriSegment.PROVIDER) String provider, String value) {
	}

	@SET(service = "example", resource = "buzz")
	public void setBuzz(@UriParam(UriSegment.PROVIDER) String provider, Long value) {
	}

	@SET(service = "example", resource = "fizzbuzz")
	public void setFizzBuzz(@UriParam(UriSegment.PROVIDER) String provider, Double value) {
	}
}

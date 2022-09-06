package org.eclipse.sensinact.prototype.notification;

import java.time.Instant;
import java.util.Objects;

/**
 * Data notifications are sent to indicate the change in the value of a resource
 * 
 * Topic name is
 * 
 * DATA/&lt;provider&gt;/&lt;service&gt;/&lt;resource&gt;
 */
public class ResourceDataNotification extends AbstractResourceNotification {
	
	public Object oldValue;
	
	public Object newValue;
	
	public Instant timestamp;

	@Override
	public String getTopic() {
		Objects.requireNonNull(provider);
		Objects.requireNonNull(service);
		Objects.requireNonNull(resource);
		return String.format("DATA/%s/%s/%s", provider, service, resource);
	}
	
}

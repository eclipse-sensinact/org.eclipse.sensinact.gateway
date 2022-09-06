package org.eclipse.sensinact.prototype.notification;

import java.time.Instant;
import java.util.Objects;

/**
 * Action notifications are sent to indicate an action triggering on a resource
 * 
 * Topic name is
 * 
 * ACTION/&lt;provider&gt;/&lt;service&gt;/&lt;resource&gt;
 */
public class ResourceActionNotification extends AbstractResourceNotification {
	
	public Instant timestamp;
	
	@Override
	public String getTopic() {
		Objects.requireNonNull(provider);
		Objects.requireNonNull(service);
		Objects.requireNonNull(resource);
		return String.format("ACTION/%s/%s/%s", provider, service, resource);
	}
	
}

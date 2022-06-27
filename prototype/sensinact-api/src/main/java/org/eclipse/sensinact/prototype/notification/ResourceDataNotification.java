package org.eclipse.sensinact.prototype.notification;

import java.time.Instant;

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
	
}

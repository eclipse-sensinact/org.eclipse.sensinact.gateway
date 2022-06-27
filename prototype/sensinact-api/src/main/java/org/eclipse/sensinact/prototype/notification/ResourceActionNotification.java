package org.eclipse.sensinact.prototype.notification;

import java.time.Instant;

/**
 * Action notifications are sent to indicate an action triggering on a resource
 * 
 * Topic name is
 * 
 * ACTION/&lt;provider&gt;/&lt;service&gt;/&lt;resource&gt;
 */
public class ResourceActionNotification extends AbstractResourceNotification {
	
	public Instant timestamp;
	
}

package org.eclipse.sensinact.prototype.notification;

/**
 * Used to register a session-based listener for resource data 
 * 
 * Events will be filtered based on the session's visibility of the resources
 */
public interface ClientActionListener {
	
	void notify(String topic, ResourceDataNotification event);

}

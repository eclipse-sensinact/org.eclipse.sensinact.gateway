package org.eclipse.sensinact.prototype.notification;

/**
 * Used to register a session-based listener for lifecycle data 
 * 
 * Events will be filtered based on the session's visibility of the resources
 */
public interface ClientLifecycleListener {
	
	void notify(String topic, LifecycleNotification event);

}

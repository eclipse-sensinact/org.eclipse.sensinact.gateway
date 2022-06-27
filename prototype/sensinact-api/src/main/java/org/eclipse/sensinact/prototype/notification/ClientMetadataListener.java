package org.eclipse.sensinact.prototype.notification;

/**
 * Used to register a session-based listener for resource metadata 
 * 
 * Events will be filtered based on the session's visibility of the resources
 */
public interface ClientMetadataListener {
	
	void notify(String topic, ResourceMetaDataNotification event);

}

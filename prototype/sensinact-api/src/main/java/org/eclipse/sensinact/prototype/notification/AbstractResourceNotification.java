package org.eclipse.sensinact.prototype.notification;

public abstract class AbstractResourceNotification {

	public String provider;
	
	public String service;
	
	public String resource;
	
	
	public abstract String getTopic();
}

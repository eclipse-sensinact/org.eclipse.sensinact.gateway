package org.eclipse.sensinact.gateway.device.openhab.common;

import java.util.HashMap;


public class Provider {

    private final String providerId;
	private final HashMap<String, Service> servicesById = new HashMap<String, Service>();
	
    public Provider(String providerId) {
        this.providerId = providerId;
    }

    public String getId() {
		return providerId;
	}
    
    public Service getOrCreateService(String serviceId) {
    	 Service service = servicesById.get(providerId);
         if (service == null) {
             service = new Service(this, serviceId);
             servicesById.put(serviceId, service);
         }
         return service;
    }
    
    public Resource getOrCreateResource(String serviceId, String resourceId) {
        Service service = getOrCreateService(serviceId);
        return service.getOrCreateResource(resourceId);
    }

    boolean setValue(String serviceId, String resourceId, final Object value) {
        Service service = getOrCreateService(serviceId);
        return service.setResourceValue(resourceId, value);
    }
    
    public void clearServices() {
        servicesById.clear();
    }
    
    @Override
    public String toString() {
        return providerId;
    }
}

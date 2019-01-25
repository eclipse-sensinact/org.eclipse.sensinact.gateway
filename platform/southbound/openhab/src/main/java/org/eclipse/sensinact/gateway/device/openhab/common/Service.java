package org.eclipse.sensinact.gateway.device.openhab.common;

import java.util.HashMap;

public class Service {

	private final Provider provider;
	private final String serviceId;
    private final HashMap<String, Resource> resourcesById = new HashMap<String, Resource>();
	    
    public Service(Provider provider, String serviceId) {
    	this.provider = provider;
        this.serviceId = serviceId;
    }

    public Provider getProvider() {
		return provider;
	}
    
    public String getId() {
		return serviceId;
	}
    
    public Resource getOrCreateResource(String resourceId) {
        Resource resource = resourcesById.get(resourceId);
        if (resource == null) {
            resource = new Resource(this, resourceId);
            resourcesById.put(resourceId, resource);
        }
        return resource;
    }
    
    public boolean setResourceValue(String resourceId, Object value) {
        boolean hasChanged;
        Resource resource = resourcesById.get(resourceId);
        if (resource == null) {
            resource = new Resource(this, resourceId, value);
            resourcesById.put(resourceId, resource);
            hasChanged = true;
        } else {
            hasChanged = resource.setValue(value);
        }
        return hasChanged;
    }
    
    public void clearResources() {
        resourcesById.clear();
    }
}

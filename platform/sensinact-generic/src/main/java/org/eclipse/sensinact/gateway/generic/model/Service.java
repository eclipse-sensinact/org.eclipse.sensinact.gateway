/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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

    public List<Resource> getResources() {
    	List<Resource> list = new ArrayList<>();
    	for (Entry<String, Resource> entity : resourcesById.entrySet())
    		list.add(entity.getValue());
    	return list;
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
    
    public Resource getOrCreateResource(String resourceId, Object value) {
    	Resource resource = getOrCreateResource(resourceId);
    	resource.setValue(value);
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

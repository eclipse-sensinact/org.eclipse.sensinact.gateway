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


public class Provider {

    private final String providerId;
	private final HashMap<String, Service> servicesById = new HashMap<String, Service>();
	
    public Provider(String providerId) {
        this.providerId = providerId;
    }

    public String getId() {
		return providerId;
	}
    
    public List<Service> getServices() {
    	List<Service> list = new ArrayList<>();
    	for (Entry<String, Service> entity : servicesById.entrySet())
    		list.add(entity.getValue());
    	return list;
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
        return getOrCreateService(serviceId).getOrCreateResource(resourceId);
    }

    boolean setValue(String serviceId, String resourceId, final Object value) {
        return getOrCreateService(serviceId).setResourceValue(resourceId, value);
    }
    
    public void clearServices() {
        servicesById.clear();
    }
    
    @Override
    public String toString() {
        return providerId;
    }
}

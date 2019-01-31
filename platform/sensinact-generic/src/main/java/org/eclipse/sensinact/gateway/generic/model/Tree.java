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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Tree {

	private final HashMap<String, Provider> providersById = new HashMap<>();

	public List<Provider> getProviders() {
		return providersById.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
	}

	public List<Service> getServices() {
		return getProviders().stream().flatMap(p -> p.getServices().stream()).collect(Collectors.toList());
	}
	
	public List<Resource> getResources() {
		return getServices().stream().flatMap(p -> p.getResources().stream()).collect(Collectors.toList());
	}
	
	public Provider getOrCreateProvider(String providerId) {
		Provider provider = providersById.get(providerId);
		if (provider == null) {
			provider = new Provider(providerId);
			providersById.put(providerId, provider);
		}
		return provider;
	}

	public Service getOrCreateService(String providerId, String serviceId) {
		return getOrCreateProvider(providerId).getOrCreateService(serviceId);
	}

	public Resource getOrCreateResource(String providerId, String serviceId, String resourceId) {
		return getOrCreateProvider(providerId).getOrCreateService(serviceId).getOrCreateResource(resourceId);
	}

	public Resource getOrCreateResource(String providerId, String serviceId, String resourceId, Object value) {
		return getOrCreateProvider(providerId).getOrCreateService(serviceId).getOrCreateResource(resourceId, value);
	}

	public boolean setValue(String providerId, String serviceId, String resourceId, Object value) {
		return getOrCreateResource(providerId, serviceId, resourceId).setValue(value);
	}
	
    public void clearProviders() {
    	providersById.clear();
    }
}

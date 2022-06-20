package org.eclipse.sensinact.prototype.model;

import java.util.Map;

/**
 * The sensiNact interface used to create and discover with the models 
 */
public interface ModelManager {
	
	ProviderBuilder createProvider(String provider);
	
	ServiceBuilder createService(String provider, String service);

	ResourceBuilder<?> createResource(String provider, String service, String resource);
	
	void deleteProvider(String provider);
	
	void deleteService(String provider, String service);

	void deleteResource(String provider, String service, String resource);
	
	Map<String, Provider> getProviders();

	Map<String, Service> getServices(String provider);

	Map<String, Resource> getResources(String provider, String service);

}

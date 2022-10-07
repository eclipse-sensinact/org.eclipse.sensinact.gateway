package org.eclipse.sensinact.prototype.model;

import java.util.Map;

/**
 * The model for a Service
 */
public interface Service extends Modelled {
	
	ResourceBuilder<?> createResource(String resource);
	
	Map<String, ? extends Resource> getResources();
	
	Provider getProvider();

}

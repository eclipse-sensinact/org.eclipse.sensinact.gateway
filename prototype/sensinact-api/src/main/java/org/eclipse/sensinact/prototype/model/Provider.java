package org.eclipse.sensinact.prototype.model;

import java.util.Map;

/**
 * A model for a Provider
 */
public interface Provider extends Modelled {
	
	ServiceBuilder createService(String service);
	
	Map<String, ? extends Service> getServices();
	
	String getModelName();

}

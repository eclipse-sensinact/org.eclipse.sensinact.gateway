package org.eclipse.sensinact.prototype.model;

/**
 * Implemented by device providers that want to programmatically register their models
 */
public interface ModelProvider {
	
	void init(ModelManager manager);
	
	void destroy();

}

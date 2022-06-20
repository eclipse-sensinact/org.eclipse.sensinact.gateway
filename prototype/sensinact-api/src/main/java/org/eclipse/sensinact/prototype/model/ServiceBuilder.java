package org.eclipse.sensinact.prototype.model;

/**
 * A builder for programmatically registering models 
 */
public interface ServiceBuilder {
	
	ServiceBuilder exclusivelyOwned(boolean exclusive);
	
	ServiceBuilder withAutoDeletion(boolean autoDelete);
	
	Service build();
}

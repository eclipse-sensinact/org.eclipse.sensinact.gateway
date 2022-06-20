package org.eclipse.sensinact.prototype.model;

/**
 * A builder for programmatically registering models 
 */
public interface ProviderBuilder {
	
	ProviderBuilder exclusivelyOwned(boolean exclusive);
	
	ProviderBuilder withAutoDeletion(boolean autoDelete);
	
	Provider build();
}

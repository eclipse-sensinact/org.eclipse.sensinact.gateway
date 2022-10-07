package org.eclipse.sensinact.prototype.command;

public interface SensinactModel extends CommandScoped {
	
	SensinactResource getOrCreateResource(String model, String provider, String service, String resource, Class<?> valueType);
}

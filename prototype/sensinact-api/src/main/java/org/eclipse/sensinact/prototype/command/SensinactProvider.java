package org.eclipse.sensinact.prototype.command;

import java.util.Map;

import org.eclipse.sensinact.prototype.model.Provider;

public interface SensinactProvider extends CommandScoped, Provider {

	Map<String, ? extends SensinactService> getServices();
	
}

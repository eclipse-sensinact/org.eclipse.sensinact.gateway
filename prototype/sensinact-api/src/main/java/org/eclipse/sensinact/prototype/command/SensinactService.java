package org.eclipse.sensinact.prototype.command;

import java.util.Map;

import org.eclipse.sensinact.prototype.model.Service;

public interface SensinactService extends CommandScoped, Service {
	
	@Override
	SensinactProvider getProvider();

	@Override
	Map<String, ? extends SensinactResource> getResources();
}

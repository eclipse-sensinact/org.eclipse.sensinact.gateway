package org.eclipse.sensinact.prototype.command;

import java.time.Instant;
import java.util.Map;

import org.eclipse.sensinact.prototype.model.Resource;
import org.osgi.util.promise.Promise;

public interface SensinactResource extends CommandScoped, Resource {
	
	default Promise<Void> setValue(Object value) {
		return setValue(value, Instant.now());
	};
	
	Promise<Void> setValue(Object value, Instant timestamp);

	Promise<Object> getValue();

	Promise<Void> setMetadataValue(String name, Object value, Instant timestamp);
	
	Promise<Object> getMetadataValue(String name);

	Promise<Map<String,Object>> getMetadataValues();
	
	@Override
	SensinactService getService();
	
}

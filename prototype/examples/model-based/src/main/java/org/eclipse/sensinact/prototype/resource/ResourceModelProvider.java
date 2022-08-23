package org.eclipse.sensinact.prototype.resource;


import java.util.Random;

import org.eclipse.sensinact.prototype.model.ModelManager;
import org.eclipse.sensinact.prototype.model.ModelProvider;
import org.osgi.service.component.annotations.Component;

@Component
public class ResourceModelProvider implements ModelProvider {

	Random random = new Random();
	@Override
	public void init(ModelManager manager) {
		manager.registerModel("fan-resource.xml");
	}

	@Override
	public void destroy() {
		// Nothing to do here as the model is auto-deleted
	}
}

package org.eclipse.sensinact.prototype.reflective;

import java.util.Random;

import org.eclipse.sensinact.prototype.model.ModelManager;
import org.eclipse.sensinact.prototype.model.ModelProvider;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.osgi.service.component.annotations.Component;

@Component
public class ProgrammaticModelProvider implements ModelProvider {

	Random random = new Random();
	@Override
	public void init(ModelManager manager) {
		manager.createProvider("reflective")
			.withAutoDeletion(true)
			.build()
			.createService("testService")
			.build()
			.createResource("testResource")
			.withType(Integer.class)
			.withGetter(() -> random.nextInt(16))
			.withValueType(ValueType.UPDATABLE);
	}

	@Override
	public void destroy() {
		// Nothing to do here as the model is auto-deleted
	}
}

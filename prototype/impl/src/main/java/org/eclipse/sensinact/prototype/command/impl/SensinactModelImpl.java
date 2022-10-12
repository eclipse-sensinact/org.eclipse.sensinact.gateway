package org.eclipse.sensinact.prototype.command.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.command.SensinactService;
import org.eclipse.sensinact.prototype.model.nexus.impl.NexusImpl;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.PromiseFactory;

public class SensinactModelImpl extends CommandScopedImpl implements SensinactModel {
	
	private final NotificationAccumulator accumulator;
	private final NexusImpl nexusImpl;
	private final PromiseFactory pf;
	
	public SensinactModelImpl(NotificationAccumulator accumulator, NexusImpl nexusImpl, PromiseFactory pf) {
		super(new AtomicBoolean(true));
		this.accumulator = accumulator;
		this.nexusImpl = nexusImpl;
		this.pf = pf;
	}

	@Override
	public SensinactResource getOrCreateResource(String model, String provider, String service, String resource,
			Class<?> valueType) {
		checkValid();
		
		SensinactProvider p = new SensinactProviderImpl(active, model, resource);
		SensinactService s = new SensinactServiceImpl(active, p, service);
		
		return new SensinactResourceImpl(active, s, resource, valueType, accumulator, nexusImpl, pf);
	}
	
}

package org.eclipse.sensinact.prototype.command.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.command.SensinactService;
import org.eclipse.sensinact.prototype.model.ResourceBuilder;

public class SensinactServiceImpl extends CommandScopedImpl implements SensinactService {

	private final SensinactProvider provider;
	private final String name;

	public SensinactServiceImpl(AtomicBoolean active, SensinactProvider provider, String name) {
		super(active);
		this.provider = provider;
		this.name = name;
	}

	@Override
	public ResourceBuilder<?> createResource(String resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, SensinactResource> getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isExclusivelyOwned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAutoDelete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SensinactProvider getProvider() {
		return provider;
	}

}

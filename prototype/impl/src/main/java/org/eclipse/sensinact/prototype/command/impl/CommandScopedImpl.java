package org.eclipse.sensinact.prototype.command.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.CommandScoped;

public abstract class CommandScopedImpl implements CommandScoped {

	protected final AtomicBoolean active;
	
	public CommandScopedImpl(AtomicBoolean active) {
		this.active = active;
	}

	public void invalidate() {
		active.set(false);
	}

	public boolean isValid() {
		return active.get();
	}
	
	protected void checkValid() {
		if(!active.get()) {
			throw new IllegalStateException("This model has been closed");
		}
	}
}

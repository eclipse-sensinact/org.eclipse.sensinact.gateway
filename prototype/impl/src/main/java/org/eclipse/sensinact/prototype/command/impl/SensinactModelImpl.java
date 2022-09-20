package org.eclipse.sensinact.prototype.command.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

public class SensinactModelImpl implements SensinactModel {
	
	private final AtomicBoolean active = new AtomicBoolean(true);
	private final NotificationAccumulator accumulator;
	
	public SensinactModelImpl(NotificationAccumulator accumulator) {
		this.accumulator = accumulator;
	}

	public void invalidate() {
		active.set(false);
	}

}

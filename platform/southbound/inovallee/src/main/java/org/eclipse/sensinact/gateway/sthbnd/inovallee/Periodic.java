/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.inovallee;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Periodic {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final ScheduledFuture<?> handle;
	
	public Periodic(Runnable run, long initialDelay, long period, TimeUnit unit) {
		handle = scheduler.scheduleAtFixedRate(run, initialDelay, period, unit);
	}
	
	public void stop() {
		handle.cancel(true);
	}
}

/*
 * Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.slf4j.osgi.logservice.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

/**
 * <code>LogServiceFactory</code> creates LogService implementations.
 */
public class LogServiceFactory implements ServiceFactory<LogService> {

	@Override
    public LogService getService(Bundle bundle, ServiceRegistration<LogService> arg1) {
        return new LogServiceImpl(bundle);
    }

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration<LogService> registration, LogService service) {
		// nothing to do.
	
	}
}

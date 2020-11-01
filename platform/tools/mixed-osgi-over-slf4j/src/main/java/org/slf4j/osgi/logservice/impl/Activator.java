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

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.service.log.LogService;

/**
 * Handles activation/deactivation of the bundle
 */
public class Activator implements BundleActivator {
    /**
     *
     * Registers a LogServiceFactory.
     *
     * @param bundleContext the framework context for the bundle
     * @throws Exception
     */
    public void start(BundleContext bundleContext) throws Exception {

        Dictionary<String,Object> props = new Hashtable<String, Object>();
        props.put("description", "An SLF4J LogService implementation.");
        ServiceFactory factory = new LogServiceFactory();
        bundleContext.registerService(LogService.class.getName(), factory, props);
    }

    /**
     *
     * Implements <code>BundleActivator.stop()</code>.
     *
     * @param bundleContext the framework context for the bundle
     * @throws Exception
     */
    public void stop(BundleContext bundleContext) throws Exception {

        // Note: It is not required that we remove the service here, since
        // the framework will do it automatically.
    }
}

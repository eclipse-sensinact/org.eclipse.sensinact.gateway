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
package org.sensinact.mqtt.server.osgi;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.sensinact.mqtt.server.MQTTException;
import org.sensinact.mqtt.server.MQTTServerService;
import org.sensinact.mqtt.server.impl.MQTTServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class Activator implements BundleActivator {//extends AbstractActivator
    private Boolean autoStart=false;
    private Integer port=1883;
    private Logger LOG = LoggerFactory.getLogger(Activator.class);
    private MQTTServerService service;
    private ServiceRegistration sr;

    public void start(BundleContext bundleContext) throws Exception {
        service = new MQTTServerImpl(bundleContext);
        if (autoStart) {
            LOG.debug("Start MQTT Service autoStart enabled.");
            try {
                LOG.info("Start MQTT Service on port {}", port);
                service.startService(port.toString());
                LOG.info("MQTT Service on port {} started.", port);
            } catch (MQTTException e) {
                LOG.warn("Failed to start MQTT Service on port {}", port);
            }
        }
        sr = bundleContext.registerService(MQTTServerService.class.getName(), service, new Hashtable<String, String>());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        sr.unregister();
        service.stopServer();
    }
    
}

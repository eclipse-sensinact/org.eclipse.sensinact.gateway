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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.*;
import org.sensinact.mqtt.server.MQTTException;
import org.sensinact.mqtt.server.MQTTServerService;
import org.sensinact.mqtt.server.impl.MQTTServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator,SynchronousConfigurationListener {
    private Boolean autoStart=false;
    private Integer port=1883;
    private Logger LOG = LoggerFactory.getLogger(Activator.class);
    private MQTTServerService service;
    private ServiceRegistration sr;
    private BundleContext bundleContext;

    public void start(final BundleContext bundleContext) throws Exception {
        this.bundleContext=bundleContext;

        //ServiceReference configadminsr=bundleContext.getServiceReferences(ConfigurationAdmin.class.getCanonicalName(),null)[0];

        bundleContext.registerService(ConfigurationListener.class.getCanonicalName(),this,new Hashtable<String,String>());

    }

    protected void startService(BundleContext bundleContext){
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

    @Override
    public void configurationEvent(ConfigurationEvent configurationEvent) {

        if(configurationEvent.getPid().equals("mqtt.server")) {

            try {

                ConfigurationAdmin configurationAdmin=bundleContext.getService(configurationEvent.getReference());
                Configuration confSensinact= configurationAdmin.getConfiguration("mqtt.server");;
                final Dictionary<String, Object> prop=confSensinact.getProperties();

                final Object autoStartString=confSensinact.getProperties().get("autoStart");
                final Object portString=confSensinact.getProperties().get("port");

                autoStart=Boolean.parseBoolean(autoStartString!=null?autoStartString.toString():"false");
                port=Integer.parseInt(portString!=null?portString.toString():"1883");

                if(prop!=null&&prop.get("port")!=null&&prop.get("autoStart")!=null){
                    startService(bundleContext);
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

        }
    }
}

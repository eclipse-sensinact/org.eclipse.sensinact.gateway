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
    private final static String CONFIGURATION_PID="mqtt.server";
    private Boolean autoStart=null;
    private Integer port=null;
    private Logger LOG = LoggerFactory.getLogger(Activator.class);
    private MQTTServerService service;
    private ServiceRegistration sr;
    private BundleContext bundleContext;

    public void start(final BundleContext bundleContext) throws Exception {
        this.bundleContext=bundleContext;

        bundleContext.registerService(SynchronousConfigurationListener.class.getCanonicalName(),this,new Hashtable<String,String>());
        updateConfig();

        service = new MQTTServerImpl(bundleContext);
        sr = bundleContext.registerService(MQTTServerService.class.getName(), service, new Hashtable<String, String>());

    }

    private void publishService(){
        if(this.autoStart){
            try {
                service.startService();
            } catch (MQTTException e) {
                LOG.error("Failed to start MQTT service",e);
            }
        }

    }

    private void updateConfig(){
        ConfigurationAdmin configurationAdmin=bundleContext.getService(bundleContext.getServiceReference(ConfigurationAdmin.class));
        Configuration mqttServerConfig= null;
        try {
            mqttServerConfig = configurationAdmin.getConfiguration(CONFIGURATION_PID,"?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(mqttServerConfig.getProperties()!=null){
            Object autoStartObject=mqttServerConfig.getProperties().get("autoStart");
            Object portObject=mqttServerConfig.getProperties().get("port");
            if(autoStartObject!=null){
                this.autoStart=Boolean.parseBoolean(autoStartObject.toString());
            }

            if(autoStartObject!=null){
                this.port=Integer.parseInt(portObject.toString());
            }

            if(this.port!=null&&this.autoStart!=null){
                publishService();
            }

        }
    }


    public void stop(BundleContext bundleContext) throws Exception {
        try  {
            sr.unregister();
        }catch (Exception e){
            LOG.warn("Failed to stop service",e);
        }

        try  {
            service.stopServer();
        }catch (Exception e){
            LOG.warn("Failed to stop service",e);
        }

    }

    @Override
    public void configurationEvent(ConfigurationEvent configurationEvent) {
        LOG.info("ConfigAdmin configuration pid {} received",configurationEvent.getPid());

        if(configurationEvent.getPid().equals(CONFIGURATION_PID)) {
            updateConfig();
        }
    }
}

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

import java.io.IOException;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.SynchronousConfigurationListener;
import org.sensinact.mqtt.server.MQTTException;
import org.sensinact.mqtt.server.MQTTServerService;
import org.sensinact.mqtt.server.impl.MQTTServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator,SynchronousConfigurationListener {
	
    private final static String CONFIGURATION_PID="mqtt.server";
    
    private Logger LOG = LoggerFactory.getLogger(Activator.class);
    
    private Boolean autoStart=null;
    private Integer port=null;
    private String host=null;
    private MQTTServerService service;
    private BundleContext bundleContext;
    
    private ServiceRegistration sr;

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
            	if(port!=null) {
            		service.startService(host,String.valueOf(port.intValue()));
            	} else {
            		service.startService();
            	}
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
            Object hostObject=mqttServerConfig.getProperties().get("host");
            if(autoStartObject!=null){
                this.autoStart=Boolean.parseBoolean(autoStartObject.toString());
            }
            if(portObject!=null){
                this.port=Integer.parseInt(String.valueOf(portObject));
            }
            if(hostObject!=null){
                this.host=String.valueOf(hostObject);
            }
            if(this.autoStart!=null && this.autoStart.booleanValue()){
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

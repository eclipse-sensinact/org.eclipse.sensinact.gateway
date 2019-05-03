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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttActivator;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.device.MqttPropertyFileConfig;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.interpolator.MqttManagedService;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Provider;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class Activator extends MqttActivator {
    private ServiceTracker mqttBusConfigFileServiceTracker;
    private ServiceTracker mqttBusPojoServiceTracker;
    private ServiceRegistration<ManagedServiceFactory> managedServiceFactory;
    private static final Logger LOG= LoggerFactory.getLogger(Activator.class);

    @Override
    public void doStart() throws Exception {
        super.doStart();
        managedServiceFactory = super.mediator.getContext().registerService(ManagedServiceFactory.class, new MqttManagedService(super.mediator.getContext()), new Hashtable<String, String>() {{
            put("service.pid", MqttManagedService.MANAGER_NAME);
        }});
        // Monitor the deployment of a Provider POJO that specifies relation between topic and provider/service/resource,
        // this is the entry point for any MQTT device
        mqttBusPojoServiceTracker = new ServiceTracker(super.mediator.getContext(), Provider.class.getName(), new MqttPojoConfigTracker((MqttProtocolStackEndpoint)endPoint, super.mediator.getContext()));
        mqttBusPojoServiceTracker.open(true);
        // Monitors the deployment of the file "mqtt-*.cfg" file to create
        mqttBusConfigFileServiceTracker = new ServiceTracker(super.mediator.getContext(), MqttPropertyFileConfig.class.getName(), new MqttPropertyFileConfigTracker(super.mediator.getContext(), (MqttProtocolStackEndpoint)endPoint));
        mqttBusConfigFileServiceTracker.open(true);
        // Smarttopic declaration
    }

    @Override
    public void doStop() {
        try {
            super.doStop();
            mqttBusConfigFileServiceTracker.close();
        }catch(Exception e){
            LOG.warn("Failed to stop SmartTopic Device",e);
        }

    }
}

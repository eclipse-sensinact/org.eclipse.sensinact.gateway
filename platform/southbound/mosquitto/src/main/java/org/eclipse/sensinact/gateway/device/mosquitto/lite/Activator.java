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
package org.eclipse.sensinact.gateway.device.mosquitto.lite;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTPropertyFileConfig;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.interpolator.MosquittoManagedService;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime.MQTTManagerRuntime;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPacket;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPojoConfigTracker;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPropertyFileConfigTracker;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Hashtable;

import static org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;

/**
 * Sensinact bundle activator
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class Activator extends AbstractActivator<Mediator>
{

    private ExtModelConfiguration MQTTDeviceFactory;
    private LocalProtocolStackEndpoint<MQTTPacket> mqttConnector;
    private ServiceRegistration registration;
    private ServiceTracker MQTTBusConfigFileServiceTracker;
    private ServiceTracker MQTTBusPojoServiceTracker;
    private ServiceRegistration<ManagedServiceFactory> managedServiceFactory;

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public void doStart() throws Exception {

        LOG.info("Starting MQTT southbound bridge..");

        MQTTDeviceFactory =new ExtModelInstanceBuilder(mediator,MQTTPacket.class)
                .withServiceBuildPolicy((byte) (BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()))
                .withResourceBuildPolicy((byte) (BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()))
                .withStartAtInitializationTime(true)
                .buildConfiguration("mosquitto-resource.xml", Collections.emptyMap());

        mqttConnector =new LocalProtocolStackEndpoint<MQTTPacket>(mediator);
        mqttConnector.connect(MQTTDeviceFactory);

        //Manages the subscription to the bus
        MQTTManagerRuntime runtime=MQTTManagerRuntime.getInstance(mqttConnector);

        this.registration = super.mediator.getContext().registerService(
                ProtocolStackEndpoint.class, mqttConnector, null);

        managedServiceFactory = super.mediator.getContext().registerService(ManagedServiceFactory.class,new MosquittoManagedService(super.mediator.getContext()),new Hashtable(){{put("service.pid", MosquittoManagedService.MANAGER_NAME);}});

        //Monitor the deployment of a Provider pojo that specifies relation between topic and provider/service/resource, this is the entry point for any MQTT device
        MQTTBusPojoServiceTracker = new ServiceTracker(super.mediator.getContext(), Provider.class.getName(), new MQTTPojoConfigTracker(runtime,super.mediator.getContext()));
        MQTTBusPojoServiceTracker.open(true);
        //Monitors the deployment of the file "mosquitto-*.cfg" file to create
        MQTTBusConfigFileServiceTracker = new ServiceTracker(super.mediator.getContext(), MQTTPropertyFileConfig.class.getName(), new MQTTPropertyFileConfigTracker(mqttConnector,super.mediator.getContext(),runtime));
        MQTTBusConfigFileServiceTracker.open(true);
        //Smarttopic declartion

        LOG.info(".. MQTT southbound bridge started.");

    }

    @Override
    public void doStop() throws Exception{

        try{
            MQTTBusConfigFileServiceTracker.close();
            mqttConnector.stop();
        }finally{
            mqttConnector = null;
            MQTTDeviceFactory = null;
        }

        try{
            managedServiceFactory.unregister();

        }finally{
            this.registration = null;
        }


        try{
            registration.unregister();

        }finally{
            registration = null;
        }
    }

    @Override
    public Mediator doInstantiate(BundleContext context)
    {
        return new Mediator(context);
    }
}

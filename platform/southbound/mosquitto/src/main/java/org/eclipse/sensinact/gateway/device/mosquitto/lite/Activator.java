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

import java.util.Collections;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTBusClientTracker;
import org.osgi.framework.BundleContext;

import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTBusClient;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPacket;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;

public class Activator extends AbstractActivator<Mediator>
{

    private ExtModelConfiguration MQTTDeviceFactory;
    private LocalProtocolStackEndpoint<MQTTPacket> MQTTConnector;
    private ServiceRegistration registration;
    private ServiceTracker MQTTBusServiceTracker;

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @SuppressWarnings("unchecked")
	@Override
    public void doStart() throws Exception 
    {
        MQTTDeviceFactory = new ExtModelInstanceBuilder(
        mediator, MQTTPacket.class
        ).withStartAtInitializationTime(true
        ).buildConfiguration("mosquitto-resource.xml", Collections.emptyMap());
        		
        MQTTConnector =new LocalProtocolStackEndpoint<MQTTPacket>(mediator);
        MQTTConnector.connect(MQTTDeviceFactory);

        this.registration = super.mediator.getContext().registerService(
                ProtocolStackEndpoint.class, MQTTConnector, null);

        MQTTBusServiceTracker = new ServiceTracker(super.mediator.getContext(), 
        		MQTTBusClient.class.getName(), 
        		new MQTTBusClientTracker(MQTTConnector,super.mediator.getContext()));

        MQTTBusServiceTracker.open(true);

    }

    @Override
    public void doStop() throws Exception{
        MQTTBusServiceTracker.close();
        MQTTConnector.stop();
        MQTTConnector = null;
        MQTTDeviceFactory = null;
        try{
            this.registration.unregister();

        }finally{
            this.registration = null;
        }
    }

    @Override
    public Mediator doInstantiate(BundleContext context) 
    {
        return new Mediator(context);
    }
}

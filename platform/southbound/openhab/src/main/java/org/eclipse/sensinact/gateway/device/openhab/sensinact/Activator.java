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
package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import org.eclipse.sensinact.gateway.device.openhab.OpenHabDevice;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import org.osgi.framework.BundleContext;

import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Properties;

public class Activator extends AbstractActivator<Mediator>
{
    private ExtModelConfiguration openHabDeviceFactory;
    public LocalProtocolStackEndpoint<OpenHabPacket> openHabConnector;
    private ServiceRegistration registration;
    private ServiceTracker openHabLampTracker;

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public void doStart() throws Exception
    {
        openHabDeviceFactory = new ExtModelInstanceBuilder(mediator, OpenHabPacket.class
            ).withStartAtInitializationTime(true
            ).buildConfiguration("openhab-resource.xml", Collections.emptyMap());
        	
        openHabConnector = new LocalProtocolStackEndpoint<OpenHabPacket>(mediator);
        openHabConnector.connect(openHabDeviceFactory);

        Dictionary prop=new Properties();
        prop.put("name","openhab");

        this.registration = super.mediator.getContext().registerService(
                ProtocolStackEndpoint.class, openHabConnector, prop);

        openHabLampTracker = new ServiceTracker(super.mediator.getContext(), 
        		OpenHabDevice.class.getName(), new LampOpenHabTracker(openHabConnector,
        				super.mediator.getContext()));

        openHabLampTracker.open(true);

    }

    @Override
    public void doStop() throws Exception{
        openHabLampTracker.close();
        openHabConnector.stop();
        openHabConnector = null;
        openHabDeviceFactory = null;
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
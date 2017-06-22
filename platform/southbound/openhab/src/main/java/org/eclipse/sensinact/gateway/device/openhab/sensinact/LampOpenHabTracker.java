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
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LampOpenHabTracker implements ServiceTrackerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private final BundleContext bundleContext;
    ProtocolStackEndpoint<OpenHabPacket> connector;

    public LampOpenHabTracker(ProtocolStackEndpoint<OpenHabPacket> connector, 
    		BundleContext bundleContext) 
    {
        this.connector=connector;
        this.bundleContext=bundleContext;
    }

    private String generateID(OpenHabDevice lamp){
        return lamp.getName();
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Attaching Lamp service");
        try{
            OpenHabDevice lamp=(OpenHabDevice)bundleContext.getService(serviceReference);
            OpenHabPacket packet = new OpenHabPacket(generateID(lamp), false);
            connector.process(packet);
            LOG.info("Sensinact Device created with the id {}",generateID(lamp));
            return bundleContext.getService(serviceReference);
        }
        catch (InvalidPacketException e){
            LOG.error("Failed to read sensinact package",e);
        }

        return null;
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        LOG.debug("Updating Lamp service");
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        LOG.info("Dettaching Lamp service");
        try
        {
            OpenHabDevice lamp=(OpenHabDevice)o;
            OpenHabPacket packet = new OpenHabPacket(generateID(lamp), false,true);
            connector.process(packet);
            LOG.info("Sensinact Device {} removed",generateID(lamp));
        }
        catch (InvalidPacketException e)
        {
            LOG.error("Failed to read sensinact package", e);
        }
    }
}

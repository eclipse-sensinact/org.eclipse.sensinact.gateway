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
package org.eclipse.sensinact.gateway.device.openhab;


import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

@Component
@Instantiate
public class OpenHabDiscovery implements ServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(OpenHabDiscovery.class);
    private static final String SERVICE_TYPE="_openhab-server._tcp.local.";
    private static final String SERVICE_NAME="openHAB";
    private final HashMap<String,ComponentInstance> openHabServers=new HashMap<String, ComponentInstance>();
    private JmDNS dns;
    private BundleContext context;

    public OpenHabDiscovery(BundleContext context){
        this.context=context;
    }

    @Requires(filter = "(factory.name=OpenHab)")
    Factory openhabFactory;

    @Validate
    public void start() throws IOException {

        String activateDiscoveryStr=context.getProperty(OpenHabDiscovery.class.getCanonicalName()+".disabled");

        Boolean desactivateDiscovery=Boolean.parseBoolean(activateDiscoveryStr);
        if(!desactivateDiscovery) {
            LOG.info("Starting openhab discovery ..");
            dns = JmDNS.create();
            dns.addServiceListener(SERVICE_TYPE, this);
            LOG.info("Starting openhab discovery STARTED");
        }else {
            LOG.warn("The openhab discovery service was disabled by system property.");
        }
    }

    @Invalidate
    public void stop(){
        LOG.warn("Stopping openhab discovery");
    }

    @Override
    public void serviceAdded(ServiceEvent event) {

        LOG.warn("Openhab device instance added. name {} type {}", event.getName(), event.getType());

        ServiceInfo info=dns.getServiceInfo(SERVICE_TYPE,SERVICE_NAME);

        final Integer port=info.getPort();

        final String ip=info.getHostAddresses()[0];

        Hashtable properties=new Hashtable();
        properties.put("ip", ip);
        properties.put("port", port);

        try {
            ComponentInstance ci=openhabFactory.createComponentInstance(properties);
            openHabServers.put(String.format("%s:%d",ip,port),ci);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            LOG.error("Wrong component configuration", unacceptableConfiguration);
        } catch (MissingHandlerException e) {
            LOG.error("Missing handlers", e);
        } catch (ConfigurationException e) {
            LOG.error("Wrong configuration", e);
        }
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {

        ServiceInfo info=dns.getServiceInfo(SERVICE_TYPE,SERVICE_NAME);

        final Integer port=info.getPort();

        final String ip=info.getHostAddresses()[0];

        ComponentInstance ci=openHabServers.remove(String.format("%s:%d", ip, port));

        if(ci!=null){
            ci.dispose();
            LOG.debug("Openhab instance removed. name {} type {}", event.getName(), event.getType());
        }else {
            LOG.warn("Failed removing openhab server instance. Server {} port {}",ip,port);
        }

    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        LOG.debug("Openhab instance resolved. name {} type {}", event.getName(), event.getType());
    }
}

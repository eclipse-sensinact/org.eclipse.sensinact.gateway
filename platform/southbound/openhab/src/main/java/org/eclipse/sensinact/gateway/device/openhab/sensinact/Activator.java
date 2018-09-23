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

import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@HttpTasks(recurrences = {@RecurrentHttpTask(delay = 1000 * 5, period = 1000 * 2, recurrence = @HttpTaskConfiguration(host = "@context[openhab.host]", port = "@context[openhab.port]", path = "/rest/items/", contentType = "application/json", acceptType = "application/json"))}, tasks = {@SimpleHttpTask(commands = {Task.CommandType.ACT}, configuration = @HttpTaskConfiguration(host = "@context[openhab.host]", port = "@context[openhab.port]", path = "/rest/items/@context[task.serviceProvider]", httpMethod = "POST", contentType = "text/plain", acceptType = "application/json", direct = true, content = Activator.OpenHabTaskConfigurator.class))})
/**
 * OpenHab2 bundle activator
 *
 * @author <a href="mailto:christophe.munillaO@cea.fr">Christophe Munilla</a>
 * @author sb252289
 */ public class Activator extends HttpActivator implements ServiceListener {
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    public static class OpenHabTaskConfigurator implements HttpTaskConfigurator {
        public OpenHabTaskConfigurator() {
        }

        /**
         * @inheritDoc
         * @see org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator#configure(org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask)
         */
        @Override
        public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
//            System.out.println("configuring task " + task);
            String leaf = UriUtils.getLeaf(task.getPath());
//            System.out.println("leaf =  " + leaf);
            String content = leaf.substring(5).toUpperCase();
//            System.out.println("content = " + content);
            task.setContent(content);
        }
    }

    private static final String OPENHAB_SERVICE_TYPE_PROPERTY_NAME = "org.eclipse.sensinact.gateway.device.openhab.type";
    private static final String OPENHAB_SERVICE_NAME_PROPERTY_NAME = "org.eclipse.sensinact.gateway.device.openhab.name";
    private static final String OPENHAB_IP_PROPERTY_NAME = "org.eclipse.sensinact.gateway.device.openhab.ip";
    private static final String OPENHAB_PORT_PROPERTY_NAME = "org.eclipse.sensinact.gateway.device.openhab.port";
    private static final String DEFAULT_OPENHAB_SERVICE_TYPE = "_openhab-server._tcp.local.";
    private static final String DEFAULT_OPENHAB_SERVICE_NAME = "openhab";
    private static final String DEFAULT_OPENHAB_IP = "127.0.0.1";
    private static final int DEFAULT_OPENHAB_PORT = 8080;
    private static final String ACTIVATE_DISCOVERY_PROPERTY_NAME = "org.eclipse.sensinact.gateway.device.openhab.OpenHabDiscovery2.disabled";
    private JmDNS dns;
    private ExtModelConfiguration<? extends HttpPacket> configuration;
    private Map<String, SimpleHttpProtocolStackEndpoint> endpoints;

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
        super.mediator.setTaskProcessingContextHandler(this.getProcessingContextHandler());
        this.mediator.setTaskProcessingContextFactory(this.getTaskProcessingContextFactory());
        this.mediator.setChainedTaskProcessingContextFactory(this.getChainedTaskProcessingContextFactory());
        this.configuration = ExtModelConfigurationBuilder.instance(mediator, getPacketType()
        		).withStartAtInitializationTime(isStartingAtInitializationTime()
        		).withServiceBuildPolicy(getServiceBuildPolicy()
        		).withResourceBuildPolicy(getResourceBuildPolicy()
        		).build(getResourceDescriptionFile(), getDefaults());
        endpoints = new HashMap<String, SimpleHttpProtocolStackEndpoint>();
        final String activateDiscoveryPropertyValue = (String) mediator.getProperty(ACTIVATE_DISCOVERY_PROPERTY_NAME);
        Boolean desactivateDiscovery = false;
        if (activateDiscoveryPropertyValue != null) {
            desactivateDiscovery = Boolean.parseBoolean(activateDiscoveryPropertyValue);
            mediator.info("Openhab2 discovery configurated by %s property set to %s", ACTIVATE_DISCOVERY_PROPERTY_NAME, desactivateDiscovery);
        } else {
            mediator.info("No openhab2 discovery configurated. Default configuration is enabled...");
        }
        final String openhabServiceTypePropertyValue = (String) mediator.getProperty(OPENHAB_SERVICE_TYPE_PROPERTY_NAME);
        String openhabServiceType = DEFAULT_OPENHAB_SERVICE_TYPE;
        if (openhabServiceTypePropertyValue != null) {
            openhabServiceType = openhabServiceTypePropertyValue;
            mediator.info("Openhab2 service type configurated by %s property set to %s", OPENHAB_SERVICE_TYPE_PROPERTY_NAME, openhabServiceType);
        } else {
            mediator.info("No openhab2 service type configurated. Using default type: " + openhabServiceType);
        }
        final String openhabServiceNamePropertyValue = (String) mediator.getProperty(OPENHAB_SERVICE_NAME_PROPERTY_NAME);
        String openhabServiceName = DEFAULT_OPENHAB_SERVICE_NAME;
        if (openhabServiceNamePropertyValue != null) {
            openhabServiceName = openhabServiceNamePropertyValue;
            mediator.info("Openhab2 service name configurated by %s property set to %s", OPENHAB_SERVICE_NAME_PROPERTY_NAME, openhabServiceName);
        } else {
            mediator.info("No openhab2 service name configurated. Using default type: " + openhabServiceName);
        }
        String openhabIP = DEFAULT_OPENHAB_IP;
        int openhabPort = DEFAULT_OPENHAB_PORT;
        if (!desactivateDiscovery) {
            mediator.info("Starting openhab2 discovery...");
            dns = JmDNS.create();
            mediator.info("...dns created...");
            dns.addServiceListener(openhabServiceType, this);
            mediator.info("...started openhab2 discovery");
            mediator.debug("...dns searching service info for type " + openhabServiceType + "...");
            ServiceInfo[] list = dns.list(openhabServiceType);
            for (ServiceInfo service : list) {
                mediator.info("...dns found openhab2 service for type " + openhabServiceType + ": " + service.getName() + " " + service);
            }
            if (list.length == 0) {
                mediator.warn("...no service info found by dns for type " + openhabServiceType);
            }
            final ServiceInfo info = dns.getServiceInfo(openhabServiceType, openhabServiceName);
            if (info == null) {
                mediator.error("among the " + list.length + " found openhab2 service(s), unable to find one available with name " + openhabServiceName);
                throw new RuntimeException("unable to find any openhab2 service available with " + openhabServiceType + " type and " + openhabServiceName + " name");
            }
            openhabPort = info.getPort();
            final String[] openhabHostAddresses = info.getHostAddresses();
            if (openhabHostAddresses.length == 0) {
                LOG.error("unexpected empty openhab2 host ip address");
            } else {
                openhabIP = info.getHostAddresses()[0];
                if (openhabHostAddresses.length > 1) {
                    LOG.warn("unexpected more than one address for openhab2 host: {}. Several openhab2 instances running?. Will use first {}", Arrays.asList(openhabHostAddresses), openhabIP);
                }
                LOG.debug("Openhab2 binded to ip {} and port {}", openhabIP, openhabPort);
            }
        } else {
            mediator.warn("The openhab2 discovery service was disabled by system property. Using openhab configuration found inside conf/config.properties:");
            final String openhabIPPropertyValue = (String) mediator.getProperty(OPENHAB_IP_PROPERTY_NAME);
            if (openhabIPPropertyValue != null) {
                openhabIP = openhabIPPropertyValue;
                mediator.info("Openhab2 ip configurated by %s property set to %s", OPENHAB_IP_PROPERTY_NAME, openhabIP);
            } else {
                openhabIP = DEFAULT_OPENHAB_IP;
                mediator.info("No openhab2 ip configurated with %s. Using default ip: %s", OPENHAB_IP_PROPERTY_NAME, openhabIP);
            }
            final String openhabPortPropertyValue = (String) mediator.getProperty(OPENHAB_PORT_PROPERTY_NAME);
            if (openhabPortPropertyValue != null) {
                openhabPort = Integer.parseInt(openhabPortPropertyValue);
                mediator.info("Openhab2 port configurated by %s property set to %s", OPENHAB_PORT_PROPERTY_NAME, openhabPort);
            } else {
                openhabPort = DEFAULT_OPENHAB_PORT;
                mediator.info("No openhab2 port configurated with %s. Using default port: ", OPENHAB_PORT_PROPERTY_NAME, openhabPort);
            }
        }
        String endpointId = "openHab".concat(String.valueOf((openhabIP + openhabPort).hashCode()));
        try {
            SimpleHttpProtocolStackEndpoint endpoint = this.configureProtocolStackEndpoint();
            endpoint.setEndpointIdentifier(endpointId);
            endpoint.connect(configuration);
            this.endpoints.put(endpointId, endpoint);
            ((OpenHabMediator) mediator).newBroker(endpointId, openhabIP, String.valueOf(openhabPort));
        } catch (Exception e) {
            mediator.error(e);
        }
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
        Iterator<SimpleHttpProtocolStackEndpoint> iterator = this.endpoints.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().stop();
        }
        this.endpoints.clear();
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        try {
            ServiceInfo info = event.getInfo();
            mediator.debug("event " + event);
            mediator.debug("event info" + event.getInfo());
            final Integer port = info.getPort();
            final String ips[] = info.getHostAddresses();
            if (ips != null && ips.length > 0) {
                String ip = ips[0];
                String endpointId = "openHab".concat(String.valueOf((ip + port.intValue()).hashCode()));
                try {
                    SimpleHttpProtocolStackEndpoint endpoint = this.configureProtocolStackEndpoint();
                    endpoint.setEndpointIdentifier(endpointId);
                    endpoint.connect(configuration);
                    this.endpoints.put(endpointId, endpoint);
                    ((OpenHabMediator) mediator).newBroker(endpointId, ip, String.valueOf(port.intValue()));
                    mediator.info("Openhab2 device instance added. name %s type %s", event.getName(), event.getType());
                } catch (Exception e) {
                    mediator.error(e);
                }
            } else {
                mediator.debug("not a new openhab2 device: " + event);
            }
        } catch (Throwable t) {
            LOG.debug("unexpected error", t);
        }
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        ServiceInfo info = event.getInfo();
        final Integer port = info.getPort();
        final String ip = info.getHostAddresses()[0];
        String endpointId = "openHab".concat(String.valueOf((ip + port.intValue()).hashCode()));
        ((OpenHabMediator) mediator).deleteBroker(endpointId);
        try {
            this.endpoints.remove(endpointId).stop();
        } catch (Exception e) {
            mediator.error(e);
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        mediator.debug("Openhab instance resolved. name %s type %s", event.getName(), event.getType());
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator#getServiceBuildPolicy()
     */
    @Override
    protected byte getServiceBuildPolicy() {
        return (byte) (BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public HttpMediator doInstantiate(BundleContext context) {
        return new OpenHabMediator(context);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator#connect(org.eclipse.sensinact.gateway.generic.ExtModelConfiguration)
     */
    @Override
    protected void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
    }
}

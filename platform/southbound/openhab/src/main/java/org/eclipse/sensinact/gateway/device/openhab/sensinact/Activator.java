/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.device.openhab.common.Broker;
import org.eclipse.sensinact.gateway.device.openhab.common.ServerLocation;
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
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HttpTasks(
    recurrences = {
        @RecurrentHttpTask(
            delay = 1000 * 5, 
            period = 1000 * 2, 
            recurrence = @HttpTaskConfiguration(
                scheme = "@context[openhab.scheme]", 
                host = "@context[openhab.host]", 
                port = "@context[openhab.port]", 
                path = "/rest/items/", 
                contentType = "application/json", 
                acceptType = "application/json"
            )
        ),
        @RecurrentHttpTask(
            delay = 1000 * 5, 
            period = 1000 * 10, 
            recurrence = @HttpTaskConfiguration(
                scheme = "@context[openhab.scheme]", 
                host = "@context[openhab.host]", 
                port = "@context[openhab.port]", 
                path = "/rest/things/", 
                contentType = "application/json", 
                acceptType = "application/json"
            )
        )
    }, 
    tasks = {
        @SimpleHttpTask(
            commands = Task.CommandType.ACT, 
            configuration = @HttpTaskConfiguration(
                scheme = "@context[openhab.scheme]", 
                host = "@context[openhab.host]", 
                port = "@context[openhab.port]", 
                path = "/rest/items/@context[task.serviceProvider]_@context[task.service]_binary", 
                httpMethod = "POST", 
                contentType = "text/plain", 
                acceptType = "application/json", 
                direct = true, 
                content = OpenHabActTaskConfigurator.class
            )
        ),
        @SimpleHttpTask(
            commands = Task.CommandType.SET, 
            configuration = @HttpTaskConfiguration(
                scheme = "@context[openhab.scheme]", 
                host = "@context[openhab.host]", 
                port = "@context[openhab.port]", 
                path = "/rest/items/@context[task.serviceProvider]_@context[task.service]_@context[task.resource]", 
                httpMethod = "POST", 
                contentType = "text/plain", 
                acceptType = "application/json", 
                direct = true, 
                content = OpenHabSetTaskConfigurator.class
            )
        )
    }
)
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends HttpActivator implements ServiceListener {

	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private ExtModelConfiguration<? extends HttpPacket> configuration;
    private Map<String, SimpleHttpProtocolStackEndpoint> endpoints;

    @Override // AbstractActivator
    public void doStart() throws Exception {
        mediator.setTaskProcessingContextHandler(getProcessingContextHandler());
        mediator.setTaskProcessingContextFactory(getTaskProcessingContextFactory());
        mediator.setChainedTaskProcessingContextFactory(getChainedTaskProcessingContextFactory());
        configuration = ExtModelConfigurationBuilder.instance(mediator, getPacketType())
        		.withStartAtInitializationTime(isStartingAtInitializationTime())
        		.withServiceBuildPolicy(getServiceBuildPolicy())
        		.withResourceBuildPolicy(getResourceBuildPolicy()
        		).build(getResourceDescriptionFile(), getDefaults());
        
        endpoints = new HashMap<String, SimpleHttpProtocolStackEndpoint>();
        List<ServerLocation> servers = OpenHabServerFinder.getServerLocation((OpenHabMediator) mediator, this);
        for(ServerLocation server : servers) {
	        try {
	        	String endpointId = buildEndpointId(server);
	            SimpleHttpProtocolStackEndpoint endpoint = configureProtocolStackEndpoint();
	            endpoint.setEndpointIdentifier(endpointId);
	            endpoint.connect(configuration);
	            endpoints.put(endpointId, endpoint);
	            ((OpenHabMediator) mediator).addBroker(endpointId, new Broker(server));
	        } catch (Exception e) {
	            LOG.error(e.getMessage(), e);
	        }
        }
    }

    @Override // AbstractActivator
    public void doStop() throws Exception {
        Iterator<SimpleHttpProtocolStackEndpoint> iterator = this.endpoints.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().stop();
        }
        this.endpoints.clear();
    }
    
    @Override // HttpActivator
    protected byte getResourceBuildPolicy() {
        return (byte) (BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());
    }

    @Override // HttpActivator
    protected byte getServiceBuildPolicy() {
        return (byte) (BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());
    }

    @Override // AbstractActivator
    public HttpMediator doInstantiate(BundleContext context) {
        return new OpenHabMediator(context);
    }

    @Override // HttpActivator
    protected void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
    }
    
    @Override // ServiceListener
    public void serviceAdded(ServiceEvent event) {
        try {
            ServiceInfo info = event.getInfo();
            LOG.debug("event " + event);
            LOG.debug("event info" + event.getInfo());
            int port = info.getPort();
            String ips[] = info.getHostAddresses();
            if (ips != null && ips.length > 0) {
                String ip = ips[0];
                ServerLocation server = new ServerLocation(ip, port);
                String endpointId = buildEndpointId(server);
                
                try {
                    SimpleHttpProtocolStackEndpoint endpoint = this.configureProtocolStackEndpoint();
                    endpoint.setEndpointIdentifier(endpointId);
                    endpoint.connect(configuration);
                    this.endpoints.put(endpointId, endpoint);
                    ((OpenHabMediator) mediator).addBroker(endpointId, new Broker(server));
                    LOG.info("Openhab2 device instance added. name %s type %s", event.getName(), event.getType());
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            } else {
                LOG.debug("not a new openhab2 device: " + event);
            }
        } catch (Throwable t) {
            LOG.debug("unexpected error", t);
        }
    }

    @Override // ServiceListener
    public void serviceRemoved(ServiceEvent event) {
        ServiceInfo info = event.getInfo();
        final Integer port = info.getPort();
        final String ip = info.getHostAddresses()[0];
        
        String endpointId = buildEndpointId(new ServerLocation(ip, port));
        ((OpenHabMediator) mediator).removeBroker(endpointId);
        try {
            this.endpoints.remove(endpointId).stop();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override // ServiceListener
    public void serviceResolved(ServiceEvent event) {
        LOG.debug("Openhab instance resolved. name %s type %s", event.getName(), event.getType());
    }
    
    private static String buildEndpointId(ServerLocation server) {
    	return "openHab" + server.hashCode();
    }
}
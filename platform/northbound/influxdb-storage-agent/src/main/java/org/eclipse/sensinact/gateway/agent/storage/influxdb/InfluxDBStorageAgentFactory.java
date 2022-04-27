/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.storage.influxdb;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.message.AgentRelay;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link ManagedServiceFactory} dedicated to InfluxDB storage agents generation
 */
@Component(immediate=true, property= {"service.pid=sensinact.influxdb.agent"})
public class InfluxDBStorageAgentFactory implements ManagedServiceFactory {

	private static final Logger LOG = LoggerFactory.getLogger(InfluxDBStorageAgentFactory.class);

	public static final String FACTORY_PID = "sensinact.influxdb.agent";
	public static final String AGENT_SERVICE_PROP = "sensinact.influxdb.agent.service";
	
	private BundleContext bundleContext;
	private Map<String,ServiceRegistration<AgentRelay>> agents;
	
	@Activate
	public void activate(ComponentContext ccontext) {
		this.bundleContext = ccontext.getBundleContext();
		this.agents = new HashMap<>();
	}

	@Override
	public String getName() {
		return FACTORY_PID;
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
		deleted(pid);	
		InfluxDBStorageAgent agent = null;
		try {
			agent = new InfluxDBStorageAgent(this.bundleContext, properties);
		}catch(Exception e) {
			LOG.error("No agent registered for {} ", pid);
			LOG.error(e.getMessage(),e);
		}
		if(agent == null)
			return;
		Hashtable<String,Object> props = new Hashtable<>();
		String measurement = String.valueOf(properties.get(InfluxDBStorageAgent.INFLUX_AGENT_MEASUREMENT_PROPS));
		if(measurement != null) 
			props.put(AGENT_SERVICE_PROP, measurement);
		ServiceRegistration<AgentRelay> registration = this.bundleContext.registerService(AgentRelay.class, 
				agent,  props);		
		this.agents.put(pid, registration);
		LOG.debug("Agent registered for {}",pid);
	}

	@Override
	public void deleted(String pid) {
		ServiceRegistration<AgentRelay> registration = this.agents.remove(pid);
		if(registration == null || registration.getReference()==null)
			return;
		AgentRelay relay = this.bundleContext.getService(registration.getReference());
		if(relay != null)
			((InfluxDBStorageAgent)relay).stop();
		try {
			registration.unregister();
			LOG.debug("Agent unregistered for {}",pid);
		} catch(IllegalStateException e) {
			LOG.error(e.getMessage(),e);
		}
	}
}

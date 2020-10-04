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
package org.eclipse.sensinact.gateway.generic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * {@link ManagedServiceFactory} implementation maintaining a set of {@link 
 * ProtocolStackEndpoint}s mapped to configuration files
 *  
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class ManagedProtocolStackEndpointFactory implements ManagedServiceFactory {
	
	/**
	 * Returns the {@link ProtocolStackEndpointConfigurator} to be used to configure
	 * a newly created {@link ProtocolStackEndpoint}
	 * 
	 * @return  the {@link ProtocolStackEndpointConfigurator} to be used
	 */
	protected abstract ProtocolStackEndpointConfigurator configurator();
	
	private Map<String, ProtocolStackEndpoint> pids;
	private Mediator mediator ;
	private final String name;
    
    /**
     * Constructor
     * 
     * @param mediator the {@link Mediator} allowing the {@link ManagedProtocolStackEndpointFactory}
     * to be created to interact with the OSGi host environment
     * @param name the name of the ManagedProtocolStackEndpointFactory to be created
     */
    public ManagedProtocolStackEndpointFactory(Mediator mediator, String name) {
    	this.mediator = mediator;
    	this.name = name;
    	this.pids = Collections.synchronizedMap(new HashMap<String,ProtocolStackEndpoint>());
    }

    /* (non-Javadoc)
     * @see org.osgi.service.cm.ManagedServiceFactory#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
     */
	@Override
    public void updated(String servicePID, Dictionary dictionary) throws ConfigurationException {
		if(this.pids.containsKey(servicePID)) {
        	deleted(servicePID);
        }
    	try {
    		SensiNactBridgeConfigurationPojo config = null;
    		ProtocolStackEndpointConfigurator configurator = configurator();
    		if(configurator!=null) {
    			config = new SensiNactBridgeConfigurationPojo(configurator);
    		} else {
    			config = new SensiNactBridgeConfigurationPojo();
    		}
    		config.populate(dictionary);
    		Class<? extends Packet> packetType = config.getPacketType();
    		if (packetType == Packet.class && !config.isOutputOnly()) {
    		    throw new NullPointerException("No packet type defined");
    		}
    		byte serviceBuildPolicy = 0;		
    		for (BuildPolicy policy : config.getServiceBuildPolicy()) {
    			serviceBuildPolicy |= policy.getPolicy();
    		}
    		byte resourceBuildPolicy = 0;
    		for (BuildPolicy policy : config.getResourceBuildPolicy()) {
    			resourceBuildPolicy |= policy.getPolicy();
    		}
    		Map<String, String> initialMap = config.getInitialProviders();
    		
    		ExtModelConfiguration configuration = 
    			ExtModelConfigurationBuilder.instance(mediator, packetType
    			    ).withServiceBuildPolicy(serviceBuildPolicy
    			    ).withResourceBuildPolicy(resourceBuildPolicy
    			    ).withStartAtInitializationTime(config.isStartAtInitializationTime()
    			    ).withObserved(Arrays.asList(config.getObserved())
    			    ).build(config.getResourceDefinition(), initialMap);

			ProtocolStackEndpoint endpoint = ReflectUtils.getInstance(
    			ProtocolStackEndpoint.class, config.getEndpointType(), mediator);
    		
    		if(configurator!=null) 
    			configurator.preConnectConfiguration(endpoint, configuration, dictionary);
    		
    		endpoint.connect(configuration);
    		
    		if(configurator!=null) 
    			configurator.postConnectConfiguration(endpoint, configuration, dictionary);
    		
            this.pids.put(servicePID, endpoint);
            
    	} catch (Exception e) {
    		e.printStackTrace();
			mediator.error(e);
		}
    }

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
	 */
	@Override
    public void deleted(String servicePID) {
    	try {
    		ProtocolStackEndpoint<?> endpoint = this.pids.remove(servicePID);
        	endpoint.stop();    		
    	} catch (Exception e) {
			mediator.error(e);
		}
    }
	
	/**
	 * Stops this ManagedProtocolStackEndpointFactory by stopping all 
	 * the maintained {@link ProtocolStackEndpoint}s
	 */
	public void stop() {
		for(ProtocolStackEndpoint<?> endpoint :this.pids.values()) {
			endpoint.stop();
		}
		this.pids.clear();
	}
}
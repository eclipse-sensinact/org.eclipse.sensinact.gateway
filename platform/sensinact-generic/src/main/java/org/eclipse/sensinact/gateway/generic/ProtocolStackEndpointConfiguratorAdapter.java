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

import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.packet.Packet;


/**
 * Empty implementation of a {@link ProtocolStackEndpointConfigurator} - Allows to override 
 * only required methods
 *  
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public class ProtocolStackEndpointConfiguratorAdapter implements ProtocolStackEndpointConfigurator {

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#preConnectConfiguration(org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint, org.eclipse.sensinact.gateway.core.ModelConfiguration, java.util.Dictionary)
	 */
	@Override
	public void preConnectConfiguration(ProtocolStackEndpoint<?> endpoint, ModelConfiguration configuration, Dictionary props) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#postConnectConfiguration(org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint, org.eclipse.sensinact.gateway.core.ModelConfiguration, java.util.Dictionary)
	 */
	@Override
	public void postConnectConfiguration(ProtocolStackEndpoint<?> endpoint, ModelConfiguration configuration, Dictionary props) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultEndpointType()
	 */
	@Override
	public Class<? extends ProtocolStackEndpoint> getDefaultEndpointType() {
		return SensiNactBridgeConfigurationPojo.DEFAULT_PROTOCOL_STACK_ENDPOINT_TYPE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultPacketType()
	 */
	@Override
	public Class<? extends Packet> getDefaultPacketType() {
		return SensiNactBridgeConfigurationPojo.DEFAULT_PACKET_TYPE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultServiceBuildPolicy()
	 */
	@Override
	public BuildPolicy[] getDefaultServiceBuildPolicy() {
		return SensiNactBridgeConfigurationPojo.DEFAULT_SERVICE_BUILD_POLICY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultResourceBuildPolicy()
	 */
	@Override
	public BuildPolicy[] getDefaultResourceBuildPolicy() {
		return SensiNactBridgeConfigurationPojo.DEFAULT_RESOURCE_BUILD_POLICY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultResourceDefinition()
	 */
	@Override
	public String getDefaultResourceDefinition() {
		return SensiNactBridgeConfigurationPojo.DEFAULT_RESOURCE_DEFINITION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultStartAtInitializationTime()
	 */
	@Override
	public boolean getDefaultStartAtInitializationTime() {
		return SensiNactBridgeConfigurationPojo.DEFAULT_START_AT_INITIALIZATION_TIME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultOutputOnly()
	 */
	@Override
	public boolean getDefaultOutputOnly() {
		return SensiNactBridgeConfigurationPojo.DEFAULT_OUTPUT_ONLY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultInitialProviders()
	 */
	@Override
	public Map<String, String> getDefaultInitialProviders() {
		return Collections.<String,String>emptyMap();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator#getDefaultObserved()
	 */
	@Override
	public String[] getDefaultObserved() {
		return new String[] {};
	}

}

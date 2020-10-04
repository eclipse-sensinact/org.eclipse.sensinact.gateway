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

import java.util.Dictionary;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.packet.Packet;

/**
 * A ProtocolStackEndpointConfigurator is in charge of finalizing
 * a {@link ProtocolStackEndpoint} configuration
 */
public interface ProtocolStackEndpointConfigurator {
	/**
	 * Process the configuration of the {@link ProtocolStackEndpoint} passed
	 * as parameter before it is connected to the {@link ModelConfiguration}
	 * also passed as parameter
	 * 
	 * @param endpoint the {@link ProtocolStackEndpoint} to finalize the configuration of
	 * @param configuration the {@link ModelConfiguration} parameterizing the 
	 * {@link ProtocolStackEndpoint}'s {@link ModelPool} in charge of instantiating
	 * new instances of the service model
	 * @param props the set of properties applying on the specified {@link 
	 * ProtocolStackEndpoint}
	 */
	void preConnectConfiguration(ProtocolStackEndpoint<?> endpoint, ModelConfiguration configuration, Dictionary props);
	
	/**
	 * Finalizes the configuration of the {@link ProtocolStackEndpoint} passed
	 * as parameter after it has been connected to the {@link ModelConfiguration}
	 * also passed as parameter
	 * 
	 * @param endpoint the {@link ProtocolStackEndpoint} to finalize the configuration of
	 * @param configuration the {@link ModelConfiguration} parameterizing the 
	 * {@link ProtocolStackEndpoint}'s {@link ModelPool} in charge of instantiating
	 * new instances of the service model
	 * @param props the set of properties applying on the specified {@link ProtocolStackEndpoint}
	 */
	void postConnectConfiguration(ProtocolStackEndpoint<?> endpoint, ModelConfiguration configuration, Dictionary props);
	
	/**
	 * Returns the {@link ProtocolStackEndpoint} type to be used if no one 
	 * is defined
	 * 
	 * @return the default {@link ProtocolStackEndpoint} type 
	 */
	Class<? extends ProtocolStackEndpoint> getDefaultEndpointType();

	/**
	 * Returns the {@link Packet} type to be used if no one 
	 * is defined
	 * 
	 * @return the default {@link Packet} type 
	 */
	Class<? extends Packet> getDefaultPacketType();

	/**
	 * Returns the {@link BuildPolicy}s array defining the build policy
	 * to be used for services if no one is defined
	 * 
	 * @return the default service build policy 
	 */
	BuildPolicy[] getDefaultServiceBuildPolicy();
	
	/**
	 * Returns the {@link BuildPolicy}s array defining the build policy
	 * to be used for resources if no one is defined
	 * 
	 * @return the default resource build policy 
	 */
	BuildPolicy[] getDefaultResourceBuildPolicy();

	/**
	 * Returns the resource definition file String path to be used if 
	 * no one is defined
	 * 
	 * @return the default resource definition file path 
	 */
	String getDefaultResourceDefinition();

	/**
	 * Returns the start at initialization time status to be used if 
	 * no one is defined
	 *  
	 * @return the default start at initialization time status 
	 */
	boolean getDefaultStartAtInitializationTime();

	/**
	 * Returns the input packet processing status to be used if 
	 * no one is defined
	 *  
	 * @return the default input packet processing status 
	 */
	boolean getDefaultOutputOnly();

	/**
	 * Returns the initial map of providers mapped to their profile to
	 * be used if no one is defined 
	 * 
	 * @return the default map of providers mapped to their profile
	 */
	Map<String, String> getDefaultInitialProviders();

	/**
	 * Returns the array of observed attribute String paths to
	 * be used if no one is defined 
	 * 
	 * @return the default array of observed attribute String paths
	 */
	String[] getDefaultObserved();
}
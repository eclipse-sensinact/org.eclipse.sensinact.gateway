/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.factory;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.config.HttpMappingProtocolStackEndpointDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint.HttpMappingProtocolStackConnectorCustomizer;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.TaskAwareHttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpChainedTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContextHandler;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpChainedTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContextHandler;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.HttpProtocolStackEndpointTasksDescription;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP ProtocolStackEndpoint generation component
 */
@Component(configurationPid = HttpMappingProtocolStackEndpointFactory.FACTORY_PID, 
	configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HttpMappingProtocolStackEndpointFactory {

	private static final Logger LOG = LoggerFactory.getLogger(HttpMappingProtocolStackEndpointFactory.class);

	public static final String FACTORY_PID = "sensinact.http.endpoint";
	
	public static final String ENDPOINT_CONFIGURATION_PROP = "sensinact.http.endpoint.config";
	public static final String ENDPOINT_RESOURCES_CONFIGURATION_PROP = "sensinact.http.endpoint.resources.config";
	public static final String ENDPOINT_TASKS_CONFIGURATION_PROP = "sensinact.http.endpoint.tasks.config";

	public static final Modifiable DEFAULT_MODIFIABLE = Modifiable.MODIFIABLE;
	
	private SimpleHttpProtocolStackEndpoint endpoint;
	
	@Activate
	void activate(BundleContext bundleContext, Map<String, ?> properties) throws Exception {
		HttpMediator mediator = new HttpMediator(bundleContext);
		mediator.setTaskProcessingContextHandler(this.getProcessingContextHandler(mediator));
        mediator.setTaskProcessingContextFactory(this.getTaskProcessingContextFactory(mediator));
        mediator.setChainedTaskProcessingContextFactory(this.getChainedTaskProcessingContextFactory(mediator));

		String endpointConfig =String.valueOf(properties.get(ENDPOINT_CONFIGURATION_PROP));
		HttpMappingProtocolStackEndpointDescription endpointDescription = null;
		
		if(endpointConfig != null) {
			File endpointConfigFile = new File(endpointConfig);
			if(endpointConfigFile.exists()) {
				try {
					FileInputStream in = new FileInputStream(endpointConfigFile);					
					ObjectMapper mapper = new ObjectMapper();
					endpointDescription = mapper.readValue(in, HttpMappingProtocolStackEndpointDescription.class);					
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
				}
			}
		}
		if(endpointDescription  == null)
			endpointDescription = new HttpMappingProtocolStackEndpointDescription();
		
		String resourcesConfig = String.valueOf(properties.get(ENDPOINT_RESOURCES_CONFIGURATION_PROP));
		if(resourcesConfig != null) {
			File resourcesConfigFile = new File(resourcesConfig);
			if(!resourcesConfigFile.exists())
				resourcesConfig = null;
		}

		HttpProtocolStackEndpointTasksDescription tasksDescription = null;
		String taskConfig = String.valueOf(properties.get(ENDPOINT_TASKS_CONFIGURATION_PROP));
		
		if(taskConfig != null) {
			File taskConfigFile = new File(taskConfig);
			if(taskConfigFile.exists()) {
				try {
					FileInputStream in = new FileInputStream(taskConfigFile);					
					ObjectMapper mapper = new ObjectMapper();
					tasksDescription = mapper.readValue(in, HttpProtocolStackEndpointTasksDescription.class);									
				} catch (IOException e) {
					LOG.error(e.getMessage(),e);
				}
			}
		}		
		
		ExtModelConfiguration<TaskAwareHttpResponsePacket> configuration = buildConfiguration(mediator, endpointDescription, 
				resourcesConfig);
		try {
			this.endpoint = new SimpleHttpProtocolStackEndpoint(mediator);
		} catch (Exception e) {
			LOG.error("Unable to create Endpoint", e);
			throw e;
		}

		if(tasksDescription != null) 
			endpoint.registerAdapters(tasksDescription);	
		
		try {
			endpoint.connect(configuration);
		} catch (InvalidProtocolStackException e) {
			LOG.error("Unable to connect Endpoint", e);
			throw e;
		}
	}

	@Deactivate
	void deactivate() {
		if(endpoint == null) 
			return;
		endpoint.stop();
	}

	private final ExtModelConfiguration<TaskAwareHttpResponsePacket> buildConfiguration(HttpMediator mediator, 
		HttpMappingProtocolStackEndpointDescription endpointDescription, String resourceConfig) {
			
		byte serviceBuildPolicy = Arrays.stream(endpointDescription.getServiceBuildPolicy())
				.map(s -> (int) BuildPolicy.valueOf(s).getPolicy())
				.reduce(0, (b1,b2) -> b1.byteValue() | b2.byteValue()).byteValue();
		
		byte resourceBuildPolicy  = Arrays.stream(endpointDescription.getResourceBuildPolicy())
				.map(s -> (int) BuildPolicy.valueOf(s).getPolicy())
				.reduce(0, (b1,b2) -> b1.byteValue() | b2.byteValue()).byteValue();
		
		Modifiable modifiable = null;
		try {
			 modifiable = Modifiable.valueOf(endpointDescription.getModifiable().toUpperCase());
		} catch(Exception e) {
			modifiable = DEFAULT_MODIFIABLE;
		}
        ExtModelConfiguration<TaskAwareHttpResponsePacket> configuration = 
        	ExtModelConfigurationBuilder.instance(
        		mediator, TaskAwareHttpResponsePacket.class
        		).withStartAtInitializationTime(endpointDescription.isStartAtInitializationTime()
        		).withServiceBuildPolicy(resourceConfig==null?BuildPolicy.BUILD_NON_DESCRIBED.getPolicy():serviceBuildPolicy
        		).withResourceBuildPolicy(resourceConfig==null?BuildPolicy.BUILD_NON_DESCRIBED.getPolicy():resourceBuildPolicy
        		).withDefaultModifiable(modifiable 
        		).withObserved(asList(endpointDescription.getObserved())
        		).build(resourceConfig, endpointDescription.getDefaults());
     
        HttpMappingProtocolStackConnectorCustomizer customizer = new HttpMappingProtocolStackConnectorCustomizer(
        		mediator, configuration, endpointDescription);
        
        configuration.setConnectorCustomizer(customizer);
        
		return configuration;
	}
	
    /**
     * Returns the {@link HttpTaskProcessingContextHandler} to be used by the {@link
     * HttpMediator} created by this factory
     *
     * @return the {@link HttpTaskProcessingContextHandler} to be used
     */
    private HttpTaskProcessingContextHandler getProcessingContextHandler(HttpMediator mediator) {
        return new DefaultHttpTaskProcessingContextHandler();
    }

    /**
     * Returns the {@link HttpTaskProcessingContextFactory} to be used to
     * create {@link HttpTaskProcessingContext}
     *
     * @return the {@link HttpTaskProcessingContextFactory} to be used
     */
    private HttpTaskProcessingContextFactory getTaskProcessingContextFactory(HttpMediator mediator) {
        return new DefaultHttpTaskProcessingContextFactory(mediator);
    }

    /**
     * Returns the {@link HttpChainedTaskProcessingContextFactory} to be used to
     * create {@link HttpTaskProcessingContext} dedicated to chained tasks
     *
     * @return the {@link HttpChainedTaskProcessingContextFactory} to be used
     */
    private HttpChainedTaskProcessingContextFactory getChainedTaskProcessingContextFactory(HttpMediator mediator) {
        return new DefaultHttpChainedTaskProcessingContextFactory(mediator);
    }

}

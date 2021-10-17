/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.config.HttpMappingProtocolStackEndpointDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint.HttpMappingProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.HttpMappingPacket;
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
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link ManagedServiceFactory} dedicated to HTTP ProtocolStackEndpoint generation
 */
@Component(immediate=true, property= {"service.pid=sensinact.http.endpoint"})
public class HttpMappingProtocolStackEndpointFactory implements ManagedServiceFactory {

	private static final Logger LOG = LoggerFactory.getLogger(HttpMappingProtocolStackEndpointFactory.class);

	public static final String FACTORY_PID = "sensinact.http.endpoint";
	
	public static final String ENDPOINT_CONFIGURATION_PROP = "sensinact.http.endpoint.config";
	public static final String ENDPOINT_RESOURCES_CONFIGURATION_PROP = "sensinact.http.endpoint.resources.config";
	public static final String ENDPOINT_TASKS_CONFIGURATION_PROP = "sensinact.http.endpoint.tasks.config";

	public static final Class<? extends HttpMappingProtocolStackEndpoint> DEFAULT_ENDPOINT_TYPE = HttpMappingProtocolStackEndpoint.class;
	public static final Class<? extends HttpMappingPacket> DEFAULT_PACKET_TYPE = HttpMappingPacket.class;
	public static final Modifiable DEFAULT_MODIFIABLE = Modifiable.MODIFIABLE;
	
	private Map<String,HttpMappingProtocolStackEndpoint> endpoints;
	private BundleContext bundleContext;
	
	@Activate
	public void activate(ComponentContext ccontext) {
		this.endpoints = new HashMap<>();
		this.bundleContext = ccontext.getBundleContext();
	}

	@Override
	public String getName() {
		return FACTORY_PID;
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
		deleted(pid);
		HttpMediator mediator = new HttpMediator(this.bundleContext);
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
		
		ExtModelConfiguration<? extends HttpPacket> configuration = buildConfiguration(mediator, endpointDescription, resourcesConfig);
		HttpMappingProtocolStackEndpoint endpoint = buildEndpoint(mediator, endpointDescription.getEndpointTypeName());
		if(endpoint == null)
			return;

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
		if(tasksDescription != null) 
			endpoint.registerAdapters(tasksDescription);		
		
		endpoint.setServiceProviderIdPattern(endpointDescription.getServiceProviderIdPattern()); 
		try {
			endpoint.connect(configuration);
			this.endpoints.put(pid,endpoint);
		} catch (InvalidProtocolStackException e) {
			LOG.error(e.getMessage(),e);
		}
	}

	@Override
	public void deleted(String pid) {
		SimpleHttpProtocolStackEndpoint endpoint = this.endpoints.remove(pid);
		if(endpoint == null) 
			return;
		endpoint.stop();
	}

	@SuppressWarnings("unchecked")
	private final ExtModelConfiguration<? extends HttpPacket> buildConfiguration(HttpMediator mediator, 
		HttpMappingProtocolStackEndpointDescription endpointDescription, String resourceConfig) {
			
		Class<? extends HttpMappingPacket> packetType = null;
		if(resourceConfig != null) {		
			try {
				packetType = (Class<? extends HttpMappingPacket>) mediator.getClassLoader(
					).loadClass(endpointDescription.getPacketTypeName());
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage(),e);
				packetType = DEFAULT_PACKET_TYPE;
			}	
		} else 
			packetType = DEFAULT_PACKET_TYPE;
			
		byte serviceBuildPolicy  = Arrays.stream(endpointDescription.getServiceBuildPolicy()).<Byte>collect(
				()->{return Byte.valueOf((byte)0);},
				(b,s)->{b = Byte.valueOf((byte)(b.byteValue() | BuildPolicy.valueOf(s).getPolicy()));},
				(b1,b2)->{b1 = Byte.valueOf((byte)(b1.byteValue() | b2.byteValue()));}).byteValue();
		
		byte resourceBuildPolicy  = Arrays.stream(endpointDescription.getResourceBuildPolicy()).<Byte>collect(
				()->{return Byte.valueOf((byte)0);},
				(b,s)->{b = Byte.valueOf((byte)(b.byteValue() | BuildPolicy.valueOf(s).getPolicy()));},
				(b1,b2)->{b1 = Byte.valueOf((byte)(b1.byteValue() | b2.byteValue()));}).byteValue();
		
		Modifiable modifiable = null;
		try {
			 modifiable = Modifiable.valueOf(endpointDescription.getModifiable().toUpperCase());
		} catch(Exception e) {
			modifiable = DEFAULT_MODIFIABLE;
		}
        ExtModelConfiguration<? extends HttpPacket> configuration = 
        	ExtModelConfigurationBuilder.instance(
        		mediator, packetType
        		).withStartAtInitializationTime(endpointDescription.isStartAtInitializationTime()
        		).withServiceBuildPolicy(resourceConfig==null?BuildPolicy.BUILD_NON_DESCRIBED.getPolicy():serviceBuildPolicy
        		).withResourceBuildPolicy(resourceConfig==null?BuildPolicy.BUILD_NON_DESCRIBED.getPolicy():resourceBuildPolicy
        		).withDefaultModifiable(modifiable 
        		).build(resourceConfig, endpointDescription.getDefaults());
		return configuration;
	}
	
	@SuppressWarnings("unchecked")
	private final HttpMappingProtocolStackEndpoint buildEndpoint(HttpMediator mediator, String endpointTypeName){
		Class<? extends HttpMappingProtocolStackEndpoint> endpointType = null;
		try {
			endpointType = (Class<? extends HttpMappingProtocolStackEndpoint>) mediator.getClassLoader().loadClass(endpointTypeName);
		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage(),e);
			endpointType = DEFAULT_ENDPOINT_TYPE;
		}			
		HttpMappingProtocolStackEndpoint endpoint = ReflectUtils.getInstance(endpointType, new Object[]{mediator});
	    return endpoint;
	}
	
    /**
     * Returns the {@link HttpTaskProcessingContextHandler} to be used by the {@link
     * HttpMediator} created by this factory
     *
     * @return the {@link HttpTaskProcessingContextHandler} to be used
     */
    protected HttpTaskProcessingContextHandler getProcessingContextHandler(HttpMediator mediator) {
        return new DefaultHttpTaskProcessingContextHandler();
    }

    /**
     * Returns the {@link HttpTaskProcessingContextFactory} to be used to
     * create {@link HttpTaskProcessingContext}
     *
     * @return the {@link HttpTaskProcessingContextFactory} to be used
     */
    public HttpTaskProcessingContextFactory getTaskProcessingContextFactory(HttpMediator mediator) {
        return new DefaultHttpTaskProcessingContextFactory(mediator);
    }

    /**
     * Returns the {@link HttpChainedTaskProcessingContextFactory} to be used to
     * create {@link HttpTaskProcessingContext} dedicated to chained tasks
     *
     * @return the {@link HttpChainedTaskProcessingContextFactory} to be used
     */
    public HttpChainedTaskProcessingContextFactory getChainedTaskProcessingContextFactory(HttpMediator mediator) {
        return new DefaultHttpChainedTaskProcessingContextFactory(mediator);
    }

}

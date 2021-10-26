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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration;
import org.eclipse.sensinact.gateway.generic.annotation.ServiceProviderDefinition;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract extended {@link AbstractActivator} implementation providing a default
 * configuration that can be amended using the {@link SensiNactBridgeConfiguration}
 * annotation
 * 
 * @param <P> the handled {@link Packet} type
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public abstract class BasisActivator<P extends Packet> extends AbstractActivator<Mediator> {
	
	private static final Logger LOG = LoggerFactory.getLogger(BasisActivator.class);
	private static final SensiNactBridgeConfiguration DEFAULT_SENSINACT_BRIDGE_CONFIGURATION = 
		new SensiNactBridgeConfiguration() {
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return SensiNactBridgeConfiguration.class;
		}

		@Override
		public String resourceDefinition() {
			return "resources.xml";
		}

		@Override
		public BuildPolicy[] serviceBuildPolicy() {
			return new  BuildPolicy[] {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};
		}

		@Override
		public BuildPolicy[] resourceBuildPolicy() {
			return new  BuildPolicy[] {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};
		}

		@Override
		public boolean startAtInitializationTime() {
			return true;
		}

		@Override
		public Class<? extends Packet> packetType() {
			return Packet.class;
		}

		@Override
		public Class<? extends ProtocolStackEndpoint> endpointType() {
			return LocalProtocolStackEndpoint.class;
		}

		@Override
		public ServiceProviderDefinition[] initialProviders() {
			return new ServiceProviderDefinition[] {};
		}

		@Override
		public String[] observed() {
			return new String[] {};
		}

		@Override
		public boolean outputOnly() {
			return false;
		}

		@Override
		public Class<? extends ServiceProviderImpl> serviceProviderType() {
			return ExtServiceProviderImpl.class;
		}

		@Override
		public Class<? extends ServiceImpl> serviceType() {
			return ExtServiceImpl.class;
		}

		@Override
		public Class<? extends ResourceImpl> resourceType() {
			return ExtResourceImpl.class;
		}
	};
	
	protected ProtocolStackEndpoint<P> endpoint;
	
	@Override
	@SuppressWarnings("unchecked")
	public void doStart() throws Exception {
		SensiNactBridgeConfiguration activation = this.getClass().getAnnotation(SensiNactBridgeConfiguration.class);
		if (activation == null) {
			activation = DEFAULT_SENSINACT_BRIDGE_CONFIGURATION;
		}
		final Class<P> packetType;
		Class<? extends Packet> clazz = activation.packetType();
		if(clazz == null) {
			throw new NullPointerException("No valid packet type defined");
		}
		if (clazz == Packet.class && !activation.outputOnly()) {
			try {
				packetType = (Class<P>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
			} catch (ClassCastException | TypeNotPresentException e) {
				LOG.error(e.getMessage(), e);
				throw new NullPointerException("No valid packet type defined");
			}
		} else {
			packetType = (Class<P>) clazz;
		}		
		byte serviceBuildPolicy = 0;		
		for (BuildPolicy policy : activation.serviceBuildPolicy()) {
			serviceBuildPolicy |= policy.getPolicy();
		}
		byte resourceBuildPolicy = 0;
		for (BuildPolicy policy : activation.resourceBuildPolicy()) {
			resourceBuildPolicy |= policy.getPolicy();
		}
		Map<String, String> initialMap = Arrays.stream(activation.initialProviders()
			).collect(Collectors.<ServiceProviderDefinition, String, String>toMap(
					p->p.name(), p->p.profileId()));
		
		Class<? extends ProtocolStackEndpoint<P>> endpointType = (Class<? extends ProtocolStackEndpoint<P>>) activation.endpointType();
		ExtModelConfiguration<P> configuration = ExtModelConfigurationBuilder.instance(mediator, packetType
			).withServiceBuildPolicy(serviceBuildPolicy
			).withResourceBuildPolicy(resourceBuildPolicy
			).withStartAtInitializationTime(activation.startAtInitializationTime()
			).withObserved(Arrays.asList(activation.observed())
			).withProviderImplementationType(activation.serviceProviderType()
			).withServiceImplementationType(activation.serviceType()
			).withResourceImplementationType(activation.resourceType()
			).build(activation.resourceDefinition(), initialMap);
				
		this.endpoint =  ReflectUtils.getInstance(ProtocolStackEndpoint.class, endpointType, mediator);
		configure();
		endpoint.connect(configuration);
	}
	
	/**
	 * Allows to finalize configuration before connecting the {@link ProtocolStackEndpoint}
	 * to the {@link Configuration} - To be overridden if necessary
	 */
	protected void configure() {
		LOG.debug("BasisActivator configuration ... done !");
	}
	
	@Override
	public void doStop() throws Exception {
		if(this.endpoint != null) {
			this.endpoint.stop();
		}
	}
	
	@Override
	public Mediator doInstantiate(BundleContext context) {
		return new Mediator(context);
	}
}

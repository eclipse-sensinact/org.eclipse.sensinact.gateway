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

/**
 * Abstract extended {@link AbstractActivator} implementation providing a default
 * configuration that can be amended using the {@link SensiNactBridgeConfiguration}
 * annotation
 * 
 * @param <P> the handled {@link Packet} type
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class BasisActivator<P extends Packet> extends AbstractActivator<Mediator> {
	
	private static final SensiNactBridgeConfiguration DEFAULT_SENSINACT_BRIDGE_CONFIGURATION = 
		new SensiNactBridgeConfiguration() {
		
		/* (non-Javadoc)
		 * @see java.lang.annotation.Annotation#annotationType()
		 */
		@Override
		public Class<? extends Annotation> annotationType() {
			return SensiNactBridgeConfiguration.class;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#resourceDefinition()
		 */
		@Override
		public String resourceDefinition() {
			return "resources.xml";
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#serviceBuildPolicy()
		 */
		@Override
		public BuildPolicy[] serviceBuildPolicy() {
			return new  BuildPolicy[] {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#resourceBuildPolicy()
		 */
		@Override
		public BuildPolicy[] resourceBuildPolicy() {
			return new  BuildPolicy[] {BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION};
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#startAtInitializationTime()
		 */
		@Override
		public boolean startAtInitializationTime() {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#packetType()
		 */
		@Override
		public Class<? extends Packet> packetType() {
			return Packet.class;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#endpointType()
		 */
		@Override
		public Class<? extends ProtocolStackEndpoint> endpointType() {
			return LocalProtocolStackEndpoint.class;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#initialProviders()
		 */
		@Override
		public ServiceProviderDefinition[] initialProviders() {
			return new ServiceProviderDefinition[] {};
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#observed()
		 */
		@Override
		public String[] observed() {
			return new String[] {};
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#outputOnly()
		 */
		@Override
		public boolean outputOnly() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#serviceProviderType()
		 */
		@Override
		public Class<? extends ServiceProviderImpl> serviceProviderType() {
			return ExtServiceProviderImpl.class;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#serviceType()
		 */
		@Override
		public Class<? extends ServiceImpl> serviceType() {
			return ExtServiceImpl.class;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration#resourceType()
		 */
		@Override
		public Class<? extends ResourceImpl> resourceType() {
			return ExtResourceImpl.class;
		}
	};
	
	protected ProtocolStackEndpoint<P> endpoint;
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStart()
	 */
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
				mediator.error(e);
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
		super.mediator.debug("BasisActivator configuration ... done !");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStop()
	 */
	@Override
	public void doStop() throws Exception {
		if(this.endpoint != null) {
			this.endpoint.stop();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
	 */
	@Override
	public Mediator doInstantiate(BundleContext context) {
		return new Mediator(context);
	}
}

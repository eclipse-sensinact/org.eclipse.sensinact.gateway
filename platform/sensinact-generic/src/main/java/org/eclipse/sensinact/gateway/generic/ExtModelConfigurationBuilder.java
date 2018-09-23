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

import java.util.Collection;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.ModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;
import org.eclipse.sensinact.gateway.core.ResourceConfigBuilder;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * Allows to build in a simple way an {@link ExtModelInstance}
 *
 * @param <C> the extended {@link ExtModelConfiguration} type in use
 * @param <I> the extended {@link ExtModelInstance} type in use
 */
public class ExtModelConfigurationBuilder<P extends Packet, C extends ExtModelConfiguration<P>,I extends ExtModelInstance<C>> extends ModelConfigurationBuilder<C,I> {
   
	public static <PACKET extends Packet> ExtModelConfigurationBuilder<PACKET,ExtModelConfiguration<PACKET>,ExtModelInstance<ExtModelConfiguration<PACKET>>> 
		instance(Mediator mediator, Class<PACKET> packetType) {
		    return new ExtModelConfigurationBuilder(mediator, ExtModelConfiguration.class, 
		    		ExtModelInstance.class, packetType);
	}

	public static ExtModelConfigurationBuilder<Packet,ExtModelConfiguration<Packet>,ExtModelInstance<ExtModelConfiguration<Packet>>>
		instance(Mediator mediator) {
		    return ExtModelConfigurationBuilder.instance(mediator,Packet.class);
	}
	
	protected Boolean lockedAtInitializationTime;
    protected Boolean isDesynchronized;
    private Class<P> packetType;
	private Class<? extends Connector<P>> connectorType;

    /**
     * @param mediator
     * @param resourceModelConfigurationType
     * @param packetType
     * 		the {@link Packet} type of the {@link Connector}
     *      to which connect the {@link SensiNactResourceModel} to build
     */
    public ExtModelConfigurationBuilder(
    	Mediator mediator, 
    	Class<C> resourceModelConfigurationType, 
    	Class<I> resourceModelInstanceType, 
    	Class<P> packetType) {
    	
        super(mediator, resourceModelConfigurationType, resourceModelInstanceType);

        this.packetType = packetType;
        super.withProviderImplementationType(ExtServiceProviderImpl.class);
        super.withServiceImplementationType(ExtServiceImpl.class);
        super.withResourceImplementationType(ExtResourceImpl.class);

        super.withServiceBuildPolicy((byte) (BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy()));
        super.withResourceBuildPolicy((byte) BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy());
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withProviderImplementationType(java.lang.Class)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withProviderImplementationType(Class<? extends ServiceProviderImpl> serviceProviderType) {
        super.withProviderImplementationType(serviceProviderType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withServiceImplementationType(java.lang.Class)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withServiceImplementationType(Class<? extends ServiceImpl> serviceType) {
        super.withServiceImplementationType(serviceType);
        return this;
    }
    
    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withUser(java.lang.String, AccessLevel)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withUser(String userPublicKey, AccessLevel accessLevel) {
        super.withUser(userPublicKey, accessLevel);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withResourceImplementationType(java.lang.Class)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withResourceImplementationType(Class<? extends ResourceImpl> resourceType) {
        super.withResourceImplementationType(resourceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withDefaultResourceType(java.lang.Class)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withDefaultResourceType(Class<? extends Resource> defaultResourceType) {
        super.withDefaultResourceType(defaultResourceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withDefaultDataType(java.lang.Class)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withDefaultDataType(Class<?> defaultDataType) {
        super.withDefaultDataType(defaultDataType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withDefaultModifiable(Modifiable)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withDefaultModifiable(Modifiable defaultModifiable) {
        super.withDefaultModifiable(defaultModifiable);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withDefaultUpdatePolicy(Resource.UpdatePolicy)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withDefaultUpdatePolicy(UpdatePolicy defaultUpdatePolicy) {
        super.withDefaultUpdatePolicy(defaultUpdatePolicy);
        return this;
    }

    /**
     * Defines the build policy applying on service instantiation
     *
     * @param buildPolicy the byte representing the {@link BuildPolicy}(s)
     *                    applying on service instantiation
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withServiceBuildPolicy(byte buildPolicy) {
        super.withServiceBuildPolicy(buildPolicy);
        return this;
    }

    /**
     * Defines the build policy applying on resource instantiation
     *
     * @param buildPolicy the byte representing the {@link BuildPolicy}(s)
     *                    applying on resource instantiation
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withResourceBuildPolicy(byte buildPolicy) {
        super.withResourceBuildPolicy(buildPolicy);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withDefaultResourceConfigBuilder(ResourceConfigBuilder)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withDefaultResourceConfigBuilder(ResourceConfigBuilder defaultResourceConfigBuilder) {
        super.withDefaultResourceConfigBuilder(defaultResourceConfigBuilder);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withStartAtInitializationTime(boolean)
     */
    @Override
    public ExtModelConfigurationBuilder<P,C,I> withStartAtInitializationTime(boolean startAtInitializationTime) {
        super.withStartAtInitializationTime(startAtInitializationTime);
        return this;
    }

    /**
     * Defines the profile to which belongs the {@link
     * SensiNactResourceModel} to be created
     *
     * @param profile the profile of the {@link
     *                SensiNactResourceModel} to build
     * @return this builder
     */
    public ExtModelConfigurationBuilder<P,C,I> withLockedAtInitializationTime(Boolean lockedAtInitializationTime) {
        this.lockedAtInitializationTime = lockedAtInitializationTime;
        return this;
    }

    /**
     * Defines the {@link Connector} type to which the {@link
     * SensiNactResourceModel} to be created will be connected to
     *
     * @param connector the {@link Connector} to which connect
     *   the {@link SensiNactResourceModel} to build
     * @return this builder
     */
    public ExtModelConfigurationBuilder<P,C,I> withConnectorType(Class<? extends Connector<P>> connectorType) {
        this.connectorType = connectorType;
        return this;
    }

    /**
     * Defines the extended {@link ServiceProviderImpl} type to use
     *
     * @param serviceProviderType the extended {@link ServiceProviderImpl} type to use
     * @return this SnaProcessorConfiguration
     */
    public ExtModelConfigurationBuilder<P,C,I> withDesynchronization(Boolean isDesynchronized) {
        this.isDesynchronized = isDesynchronized;
        return this;
    }

    /**
     * Attaches a new String path, starting from the service definition, of
     * an attribute to be observed by the {@link ModelInstanceRegistration}
     * of a {@link ExtModelInstance} built by this ExtModelInstanceBuilder
     *
     * @param observed the String path of an attribute to be observed
     */
    public ExtModelConfigurationBuilder<P,C,I> withObserved(String observed) {
        super.withObserved(observed);
        return this;
    }

    /**
     * Attaches a the collection of String paths, starting from services definition,
     * of attributes to be observed by the {@link ModelInstanceRegistration}
     * of a {@link ExtModelInstance} built by this ExtModelInstanceBuilder
     *
     * @param observed the collection of String paths of attributes to be observed
     */
    public ExtModelConfigurationBuilder<P,C,I> withObserved(Collection<String> observed) {
        super.withObserved(observed);
        return this;
    }
	
    /**
     * Configures the {@link ExtModelConfiguration} passed as parameter
     *
     * @param configuration the {@link ExtModelConfiguration} to
     *                      configure
     */
    @Override
    protected void configure(C configuration) {
    	if(configuration == null) {
    		return;
    	}
        super.configure(configuration);

        if (this.isDesynchronized != null) {
            ((ExtModelConfiguration<P>) configuration).setDesynchronized(isDesynchronized);
        }
        if (this.lockedAtInitializationTime != null) {
            ((ExtModelConfiguration<P>) configuration).setLockedAtInitializationTime(lockedAtInitializationTime);
        }
        if (this.connectorType != null) {
            ((ExtModelConfiguration<P>) configuration).setConnectorType(this.connectorType);
        }
    }

    /**
     * Creates and returns a {@link ModelConfiguration}
     * instance with the specified properties.
     *
     * @return the new created {@link ModelConfiguration}
     */
    @Override
    public C build(Object... parameters) {
        C configuration = null;
        AccessTree<? extends AccessNode> accessTree = super.buildAccessTree();

        int parametersLength = (parameters == null ? 0 : parameters.length);
        int offset = (super.defaultResourceConfigBuilder != null) ? 4 : 3;
        Object[] arguments = new Object[parametersLength + offset];
        if (parametersLength > 0) {
            System.arraycopy(parameters, 0, arguments, offset, parametersLength);
        }
        arguments[0] = mediator;
        arguments[1] = accessTree;
        arguments[2] = packetType;
        if (this.defaultResourceConfigBuilder != null) {
            arguments[3] = defaultResourceConfigBuilder;
        }
        configuration = ReflectUtils.<ExtModelConfiguration, C>getInstance(
            ExtModelConfiguration.class, (Class<C>) this.modelConfigurationType, arguments);
        this.configure(configuration);
        return configuration;
    }
}

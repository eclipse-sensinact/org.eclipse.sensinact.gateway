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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.ModelAlreadyRegisteredException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ModelInstanceBuilder;
import org.eclipse.sensinact.gateway.core.ModelInstanceRegistration;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;
import org.eclipse.sensinact.gateway.core.ResourceConfigBuilder;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessProfile;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

import java.util.Collection;

/**
 * Allows to build in a simple way an {@link ExtModelInstance}
 *
 * @param <C> the extended {@link ExtModelConfiguration} type in use
 * @param <I> the extended {@link ExtModelInstance} type in use
 */
public class ExtModelInstanceBuilder extends ModelInstanceBuilder {
    protected Boolean lockedAtInitializationTime;
    protected Boolean isDesynchronized;
    private Connector<? extends Packet> connector;
    private Class<? extends Packet> packetType;

    /**
     * @param mediator
     * @param packetType the {@link Packet} type of the {@link Connector}
     *                   to which connect the {@link SensiNactResourceModel} to build
     */
    public ExtModelInstanceBuilder(Mediator mediator, Class<? extends Packet> packetType) {
        <ExtModelConfiguration, ExtModelInstance>this(mediator, ExtModelInstance.class, ExtModelConfiguration.class, packetType);
    }

    /**
     * @param mediator
     * @param resourceModelType
     * @param resourceModelConfigurationType
     * @param packetType                     the {@link Packet} type of the {@link Connector}
     *                                       to which connect the {@link SensiNactResourceModel} to build
     */
    public <C extends ExtModelConfiguration, I extends ExtModelInstance<C>> ExtModelInstanceBuilder(Mediator mediator, Class<I> resourceModelType, Class<C> resourceModelConfigurationType, Class<? extends Packet> packetType) {
        super(mediator, resourceModelType, resourceModelConfigurationType);

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
    public ExtModelInstanceBuilder withProviderImplementationType(Class<? extends ServiceProviderImpl> serviceProviderType) {
        super.withProviderImplementationType(serviceProviderType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withServiceImplementationType(java.lang.Class)
     */
    @Override
    public ExtModelInstanceBuilder withServiceImplementationType(Class<? extends ServiceImpl> serviceType) {
        super.withServiceImplementationType(serviceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withAccessProfile(AccessProfile)
     */
    @Override
    public ExtModelInstanceBuilder withAccessProfile(AccessProfile accessProfile) {
        super.withAccessProfile(accessProfile);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withUser(java.lang.String, AccessLevel)
     */
    @Override
    public ExtModelInstanceBuilder withUser(String userPublicKey, AccessLevel accessLevel) {
        super.withUser(userPublicKey, accessLevel);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#withResourceImplementationType(java.lang.Class)
     */
    @Override
    public ExtModelInstanceBuilder withResourceImplementationType(Class<? extends ResourceImpl> resourceType) {
        super.withResourceImplementationType(resourceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withDefaultResourceType(java.lang.Class)
     */
    @Override
    public ExtModelInstanceBuilder withDefaultResourceType(Class<? extends Resource> defaultResourceType) {
        super.withDefaultResourceType(defaultResourceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withDefaultDataType(java.lang.Class)
     */
    @Override
    public ExtModelInstanceBuilder withDefaultDataType(Class<?> defaultDataType) {
        super.withDefaultDataType(defaultDataType);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withDefaultModifiable(Modifiable)
     */
    @Override
    public ExtModelInstanceBuilder withDefaultModifiable(Modifiable defaultModifiable) {
        super.withDefaultModifiable(defaultModifiable);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withDefaultUpdatePolicy(Resource.UpdatePolicy)
     */
    @Override
    public ExtModelInstanceBuilder withDefaultUpdatePolicy(UpdatePolicy defaultUpdatePolicy) {
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
    public ExtModelInstanceBuilder withServiceBuildPolicy(byte buildPolicy) {
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
    public ExtModelInstanceBuilder withResourceBuildPolicy(byte buildPolicy) {
        super.withResourceBuildPolicy(buildPolicy);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withDefaultResourceConfigBuilder(ResourceConfigBuilder)
     */
    @Override
    public ExtModelInstanceBuilder withDefaultResourceConfigBuilder(ResourceConfigBuilder defaultResourceConfigBuilder) {
        super.withDefaultResourceConfigBuilder(defaultResourceConfigBuilder);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withStartAtInitializationTime(boolean)
     */
    @Override
    public ExtModelInstanceBuilder withStartAtInitializationTime(boolean startAtInitializationTime) {
        super.withStartAtInitializationTime(startAtInitializationTime);
        return this;
    }

    /**
     * @inheritDoc
     * @see ModelInstanceBuilder#
     * withConfiguration(ModelConfiguration)
     */
    @Override
    public ExtModelInstanceBuilder withConfiguration(ModelConfiguration modelConfiguration) {
        super.withConfiguration(modelConfiguration);
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
    public ExtModelInstanceBuilder withLockedAtInitializationTime(Boolean lockedAtInitializationTime) {
        this.lockedAtInitializationTime = lockedAtInitializationTime;
        return this;
    }

    /**
     * Defines the {@link Connector} to which the {@link
     * SensiNactResourceModel} to be created will be connected to
     *
     * @param connector the {@link Connector} to which connect
     *                  the {@link SensiNactResourceModel} to build
     * @return this builder
     */
    public ExtModelInstanceBuilder withConnector(Connector<?> connector) {
        this.connector = connector;
        return this;
    }

    /**
     * Defines the extended {@link ServiceProviderImpl} type to use
     *
     * @param serviceProviderType the extended {@link ServiceProviderImpl} type to use
     * @return this SnaProcessorConfiguration
     */
    public ExtModelInstanceBuilder withDesynchronization(Boolean isDesynchronized) {
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
    public ExtModelInstanceBuilder withObserved(String observed) {
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
    public ExtModelInstanceBuilder withObserved(Collection<String> observed) {
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
    protected <C extends ModelConfiguration> void configure(C configuration) {
        super.configure(configuration);

        if (this.isDesynchronized != null) {
            ((ExtModelConfiguration) configuration).setDesynchronized(isDesynchronized);
        }
        if (this.lockedAtInitializationTime != null) {
            ((ExtModelConfiguration) configuration).setLockedAtInitializationTime(lockedAtInitializationTime);
        }
    }

    /**
     * Creates and returns a {@link ModelConfiguration}
     * instance with the specified properties.
     *
     * @return the new created {@link ModelConfiguration}
     */
    @Override
    public <C extends ModelConfiguration> C buildConfiguration(Object... parameters) {
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
        configuration = ReflectUtils.<ModelConfiguration, C>getInstance(ModelConfiguration.class, (Class<C>) this.resourceModelConfigurationType, arguments);

        if (configuration != null) {
            this.configure(configuration);
            this.withConfiguration(configuration);
        }
        return configuration;
    }

    /**
     * Creates and return a {@link SensiNactResourceModel}
     * instance with the specified properties. Optional arguments
     * apply to the {@link SensiNactResourceModelConfiguration}
     * initialization
     *
     * @return the new created {@link SensiNactResourceModel}
     */
    @Override
    public <C extends ModelConfiguration, I extends ModelInstance<C>> I build(String name, String profileId, Object... parameters) {
        I instance = null;
        if (super.modelConfiguration == null) {
            super.buildConfiguration(parameters);
        }
        if (super.modelConfiguration != null) {
            super.buildAccessNode(super.modelConfiguration.getAccessTree(), name);

            instance = ReflectUtils.<ModelInstance, I>getInstance(ModelInstance.class, (Class<I>) this.resourceModelType, this.mediator, this.modelConfiguration, name, profileId, this.connector);
            try {
                super.register(instance);

            } catch (ModelAlreadyRegisteredException e) {
                mediator.error("Model instance '%s' already exists", name);
                instance = null;
            }
        }
        return instance;
    }
}

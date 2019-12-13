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
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.api.core.Resource;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * Allows to build a {@link ModelInstance} in a simple way
 */
public class ModelConfigurationBuilder<C extends ModelConfiguration,I extends ModelInstance<C>> {
	protected Mediator mediator;
	protected Class<C> modelConfigurationType;
	protected Class<I> modelInstanceType;

	protected Class<? extends ResourceImpl> resourceType;
	protected Class<? extends ServiceProviderImpl> serviceProviderType;
	protected Class<? extends ServiceImpl> serviceType;

	private Class<? extends Resource> defaultResourceType;
	private Class<?> defaultDataType;
	private Modifiable defaultModifiable;
	private Resource.UpdatePolicy defaultUpdatePolicy;

	protected boolean startAtInitializationTime = false;

	// default behavior : refer to the xml description to instantiate all
	// pre-defined resources
	protected byte resourceBuildPolicy = (byte) (BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.getPolicy()
			| BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());

	// default behavior : instantiate services whose name is returned
	// by the initial ServiceEnumeration request only
	protected byte serviceBuildPolicy = (byte) (BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.getPolicy()
			| BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());

	protected Map<String, AccessLevel> users;
	protected ResourceConfigBuilder defaultResourceConfigBuilder;
	protected ArrayList<String> observed;

	/**
	 * @param resourceModelType
	 * @param name
	 */
	public ModelConfigurationBuilder(
			Mediator mediator,
			Class<C> resourceModelConfigurationType, 
			Class<I> resourceModelInstanceType) {
		this.mediator = mediator;
		this.modelConfigurationType = resourceModelConfigurationType;
		this.modelInstanceType = resourceModelInstanceType;
		this.users = new HashMap<String, AccessLevel>();
	}

	/**
	 * Defines the extended {@link ServiceProviderImpl} type to use
	 * 
	 * @param serviceProviderType
	 *            the extended {@link ServiceProviderImpl} type to use
	 * @return this SnaProcessorConfiguration
	 */
	public ModelConfigurationBuilder<C,I> withProviderImplementationType(
			Class<? extends ServiceProviderImpl> serviceProviderType) {
		this.serviceProviderType = serviceProviderType;
		return this;
	}

	/**
	 * Defines the extended {@link ServiceImpl} type to use
	 * 
	 * @param serviceType
	 *            the extended {@link ServiceImpl} type to use
	 */
	public ModelConfigurationBuilder<C,I> withServiceImplementationType(Class<? extends ServiceImpl> serviceType) {
		this.serviceType = serviceType;
		return this;
	}

	/**
	 * Specifies a specific {@link AccessLevel} for the user whose public key is
	 * passed as parameter
	 * 
	 * @param userPublicKey
	 *            the user's public key
	 * @param accessLevel
	 *            the {@link AccessLevel} for the specified user
	 */
	public ModelConfigurationBuilder<C,I> withUser(String userPublicKey, AccessLevel accessLevel) {
		this.users.put(userPublicKey, accessLevel);
		return this;
	}

	/**
	 * Defines the extended {@link ResourceImpl} type to use
	 * 
	 * @param resourceType
	 *            the extended {@link ResourceImpl} type to use
	 */
	public ModelConfigurationBuilder<C,I> withResourceImplementationType(Class<? extends ResourceImpl> resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	/**
	 * Sets the default extended {@link Resource} interface to be used by the
	 * {@link ResourceConfigBuilder} of the {@link ModelConfiguration} to be created
	 * and/or configured by this ModelInstanceBuilder
	 * 
	 * @param defaultResourceType
	 *            the default extended {@link Resource} interface to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelConfigurationBuilder<C,I> withDefaultResourceType(Class<? extends Resource> defaultResourceType) {
		this.defaultResourceType = defaultResourceType;
		return this;
	}

	/**
	 * Sets the default data Type to be used by the {@link ResourceConfigBuilder} of
	 * the {@link ModelConfiguration} to be created and/or configured by this
	 * ModelInstanceBuilder
	 * 
	 * @param defaultDataType
	 *            the default data Type to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelConfigurationBuilder<C,I> withDefaultDataType(Class<?> defaultDataType) {
		this.defaultDataType = defaultDataType;
		return this;
	}

	/**
	 * Sets the default {@link Modifiable} to be used by the
	 * {@link ResourceConfigBuilder} of the {@link ModelConfiguration} to be created
	 * and/or configured by this ModelInstanceBuilder
	 * 
	 * @param defaultModifiable
	 *            the default {@link Modifiable} to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelConfigurationBuilder<C,I> withDefaultModifiable(Modifiable defaultModifiable) {
		this.defaultModifiable = defaultModifiable;
		return this;
	}

	/**
	 * Sets the default extended {@link Resource.UpdatePolicy} to be used by the
	 * {@link ResourceConfigBuilder} of the {@link ModelConfiguration} to be created
	 * and/or configured by this ModelInstanceBuilder
	 * 
	 * @param defaultUpdatePolicy
	 *            the default {@link Resource.UpdatePolicy} to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelConfigurationBuilder<C,I> withDefaultUpdatePolicy(Resource.UpdatePolicy defaultUpdatePolicy) {
		this.defaultUpdatePolicy = defaultUpdatePolicy;
		return this;
	}

	/**
	 * Defines the build policy applying on service instantiation
	 * 
	 * @param buildPolicy
	 *            the byte representing the {@link BuildPolicy}(s) applying on
	 *            service instantiation
	 * 
	 */
	public ModelConfigurationBuilder<C,I> withServiceBuildPolicy(byte buildPolicy) {
		this.serviceBuildPolicy = buildPolicy;
		return this;
	}

	/**
	 * Defines the build policy applying on resource instantiation
	 * 
	 * @param buildPolicy
	 *            the byte representing the {@link BuildPolicy}(s) applying on
	 *            resource instantiation
	 * 
	 */
	public ModelConfigurationBuilder<C,I> withResourceBuildPolicy(byte buildPolicy) {
		this.resourceBuildPolicy = buildPolicy;
		return this;
	}

	/**
	 * Defines the default {@link ResourceConfigCatalog} providing the available
	 * {@link ResourceConfig}s
	 * 
	 * @param resourceConfigCatalog
	 *            the {@link ResourceConfigBuilder} to be set
	 */
	public ModelConfigurationBuilder<C,I> withDefaultResourceConfigBuilder(ResourceConfigBuilder defaultResourceConfigBuilder) {
		this.defaultResourceConfigBuilder = defaultResourceConfigBuilder;
		return this;
	}

	/**
	 * Defines whether the resource model is build dynamically according to the
	 * content of a parsed communication packet
	 * 
	 * @param buildDynamically
	 *            <ul>
	 *            <li>true if the resource model has to be build dynamically
	 *            according to the content of a parsed communication packet</li>
	 *            <li>false otherwise</li>
	 *            </ul>
	 */
	public ModelConfigurationBuilder withStartAtInitializationTime(boolean startAtInitializationTime) {
		this.startAtInitializationTime = startAtInitializationTime;
		return this;
	}

	/**
	 * Attaches a new String path, starting from the service definition, of an
	 * attribute to be observed by the {@link ModelInstanceRegistration} of a
	 * {@link ModelInstance} built by this ModelInstanceBuilder
	 * 
	 * @param observed
	 *            the String path of an attribute to be observed
	 */
	public ModelConfigurationBuilder<C,I> withObserved(String observed) {
		if (this.observed == null) {
			this.observed = new ArrayList<String>();
		}
		this.observed.add(observed);
		return this;
	}

	/**
	 * Attaches a the collection of String paths, starting from services definition,
	 * of attributes to be observed by the {@link ModelInstanceRegistration} of a
	 * {@link ModelInstance} built by this ModelInstanceBuilder
	 * 
	 * @param observed
	 *            the collection of String paths of attributes to be observed
	 */
	public ModelConfigurationBuilder<C,I> withObserved(Collection<String> observed) {
		if (this.observed == null) {
			this.observed = new ArrayList<String>();
		}
		this.observed.addAll(observed);
		return this;
	}

	/**
	 * Configures the {@link ModelConfiguration} passed as parameter
	 * 
	 * @param configuration
	 *            the {@link ModelConfiguration} to configure
	 */
	protected void configure(C configuration) {
		if(configuration == null) {
			return;
		}
		configuration.setServiceBuildPolicy(this.serviceBuildPolicy
				).setResourceBuildPolicy(this.resourceBuildPolicy
				).setStartAtInitializationTime(this.startAtInitializationTime);

		if (this.resourceType != null) {
			configuration.setResourceImplementationType(this.resourceType);
		}
		if (this.serviceType != null) {
			configuration.setServiceImplmentationType(this.serviceType);
		}
		if (this.serviceProviderType != null) {
			configuration.setProviderImplementationType(this.serviceProviderType);
		}
		if (this.defaultResourceType != null) {
			configuration.setDefaultResourceType(this.defaultResourceType);
		}
		if (this.defaultDataType != null) {
			configuration.setDefaultDataType(this.defaultDataType);
		}
		if (this.defaultModifiable != null) {
			configuration.setDefaultModifiable(this.defaultModifiable);
		}
		if (this.defaultUpdatePolicy != null) {
			configuration.setDefaultUpdatePolicy(this.defaultUpdatePolicy);
		}
		if (this.observed != null) {
			configuration.setObserved(this.observed);
		}
		if (this.modelInstanceType != null) {
			configuration.setModelInstanceType(this.modelInstanceType);
		}
	}

	/**
	 * Creates and returns the {@link RootNode} of the {@link AccessNodeImpl}s
	 * hierarchy for the {@link SensiNactResourceModel}(s) to be built by the
	 * intermediate of this builder
	 * 
	 * @return the {@link RootNode} of the {@link AccessNodeImpl}s hierarchy for the
	 *         {@link SensiNactResourceModel}(s) to be built
	 */
	protected AccessTree<?> buildAccessTree() {
		return AccessController.<AccessTree<?>>doPrivileged(new PrivilegedAction<AccessTree<?>>() {
			@Override
			public AccessTree<?> run() {
				final String identifier = ModelConfigurationBuilder.this.mediator.callService(BundleValidation.class,
					new Executable<BundleValidation, String>() {
						@Override
						public String execute(BundleValidation service) throws Exception {
							return service.check(ModelConfigurationBuilder.this.mediator.getContext().getBundle());
						}
					});
				AccessTree<?> tree = null;
				if (identifier == null) {
					tree = new AccessTreeImpl<>(mediator).withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
				} else {
					tree = ModelConfigurationBuilder.this.mediator.callService(SecuredAccess.class,
						new Executable<SecuredAccess, AccessTree<?>>() {
							@Override
							public AccessTree<?> execute(SecuredAccess service) throws Exception {
								return service.getAccessTree(identifier);
							}
						});
				}
				return tree;
			}
		});
	}

	/**
	 * Creates and returns a {@link ModelConfiguration} instance with the specified
	 * properties.
	 * 
	 * @return the new created {@link ModelConfiguration}
	 */
	public C build(Object... parameters) {
		C configuration = null;
		AccessTree<?> accessTree = this.buildAccessTree();

		int parametersLength = (parameters == null ? 0 : parameters.length);
		int offset = (this.defaultResourceConfigBuilder != null) ? 3 : 2;
		Object[] arguments = new Object[parametersLength + offset];
		if (parametersLength > 0) {
			System.arraycopy(parameters, 0, arguments, offset, parametersLength);
		}
		arguments[0] = mediator;
		arguments[1] = accessTree;

		if (this.defaultResourceConfigBuilder != null) {
			arguments[2] = defaultResourceConfigBuilder;
		}
		configuration = ReflectUtils.<ModelConfiguration, C>getInstance(
			ModelConfiguration.class, this.modelConfigurationType, arguments);

		this.configure(configuration);
		return configuration;
	}
}
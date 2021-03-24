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
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.core.security.MutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;

/**
 * Configuration of a sensiNact Resource Model instance
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ModelConfiguration implements SensiNactResourceModelConfiguration {
	protected Class<? extends ServiceProviderImpl> serviceProviderType = null;
	private final Class<? extends ServiceProviderImpl> defaultServiceProviderType;

	protected Class<? extends ServiceImpl> serviceType = null;
	private final Class<? extends ServiceImpl> defaultServiceType;

	protected Class<? extends ResourceImpl> resourceType = null;
	private final Class<? extends ResourceImpl> defaultResourceType;
	
	protected Class<? extends ModelInstance<?>> modelInstanceType;

	private ResourceConfigCatalogCollection resourceConfigCatalogs = null;
	private ResourceConfigBuilder defaultResourceConfigBuilder = null;

	private Class<? extends ResourceConfig> resourceConfigType = null;

	protected byte resourceBuildPolicy = 0;
	protected byte serviceBuildPolicy = 0;
	private boolean startAtInitializationTime;

	protected Map<String, List<String>> profiles;
	protected Map<String, List<String>> fixed;
	protected List<String> observed;
	
	/**
	 * the {@link AccessTree} of the {@link AccessProfile}s hierarchy describing the
	 * access policy applying on this sensiNact resource model
	 */
	protected final MutableAccessTree<? extends MutableAccessNode> accessTree;
	protected Mediator mediator;
	

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the ModelConfiguration to be
	 *            instantiated to interact with the OSGi host environment
	 * @param accessTree
	 *            the {@link AccessTree} defining the access rights applying on the
	 *            instances of the data model based on the ModelConfiguration to be
	 *            instantiated
	 */
	public ModelConfiguration(Mediator mediator, MutableAccessTree<? extends MutableAccessNode> accessTree) {
		this(mediator, accessTree, new DefaultResourceConfigBuilder());
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the ModelConfiguration to be
	 *            instantiated to interact with the OSGi host environment
	 * @param accessTree
	 *            the {@link AccessTree} defining the access rights applying on the
	 *            instances of the data model based on the ModelConfiguration to be
	 *            instantiated
	 * @param defaultResourceConfigBuilder
	 *            the {@link ResourceConfigBuilder} to be used by default to
	 *            instantiate new {@link ResourceConfig}s
	 */
	public ModelConfiguration(Mediator mediator, MutableAccessTree<? extends MutableAccessNode> accessTree,
			ResourceConfigBuilder defaultResourceConfigBuilder) {
		this(mediator, 
			accessTree, 
			defaultResourceConfigBuilder, 
			ServiceProviderImpl.class, 
			ServiceImpl.class,
			ResourceImpl.class);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the ModelConfiguration to be
	 *            instantiated to interact with the OSGi host environment
	 * @param accessTree
	 *            the {@link AccessTree} defining the access rights applying on the
	 *            instances of the data model based on the ModelConfiguration to be
	 *            instantiated
	 * @param defaultServiceProviderType
	 *            the extended {@link ServiceProviderImpl} type to define as the
	 *            default
	 * @param defaultServiceType
	 *            the extended {@link ServiceImpl} type to define as the default
	 *            service one
	 * @param defaultResourceType
	 *            the extended {@link ResourceImpl} type to define as the default
	 *            resource one
	 */
	protected ModelConfiguration(
			Mediator mediator, 
			MutableAccessTree<? extends MutableAccessNode> accessTree,
			Class<? extends ServiceProviderImpl> defaultServiceProviderType,
			Class<? extends ServiceImpl> defaultServiceType, 
			Class<? extends ResourceImpl> defaultResourceType) {
		this(mediator, 
			accessTree, 
			new DefaultResourceConfigBuilder(), 
			defaultServiceProviderType, 
			defaultServiceType,
			defaultResourceType);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the ModelConfiguration to be
	 *            instantiated to interact with the OSGi host environment
	 * @param accessTree
	 *            the {@link AccessTree} defining the access rights applying on the
	 *            instances of the data model based on the ModelConfiguration to be
	 *            instantiated
	 * @param defaultResourceConfigBuilder
	 *            the {@link ResourceConfigBuilder} to be used by default to
	 *            instantiate new {@link ResourceConfig}s
	 * @param defaultServiceProviderType
	 *            the extended {@link ServiceProviderImpl} type to define as the
	 *            default service provider one
	 * @param defaultServiceType
	 *            the extended {@link ServiceImpl} type to define as the default
	 *            service one
	 * @param defaultResourceType
	 *            the extended {@link ResourceImpl} type to define as the default
	 *            resource one
	 */
	protected ModelConfiguration(Mediator mediator, MutableAccessTree<? extends MutableAccessNode> accessTree,
			ResourceConfigBuilder defaultResourceConfigBuilder,
			Class<? extends ServiceProviderImpl> defaultServiceProviderType,
			Class<? extends ServiceImpl> defaultServiceType, Class<? extends ResourceImpl> defaultResourceType) {
		this.mediator = mediator;
		this.accessTree = accessTree;
		this.defaultServiceProviderType = defaultServiceProviderType;
		this.defaultServiceType = defaultServiceType;
		this.defaultResourceType = defaultResourceType;
		this.resourceConfigCatalogs = new ResourceConfigCatalogCollection();

		ServiceLoader<ResourceConfigCatalog> loader = ServiceLoader.load(ResourceConfigCatalog.class, mediator.getClassLoader());
		Iterator<ResourceConfigCatalog> iterator = loader.iterator();
		while (iterator.hasNext()) {
			ResourceConfigCatalog catalog = iterator.next();
			if (catalog != null) 
				this.resourceConfigCatalogs.add(catalog);			
		}
		this.resourceConfigType = ResourceConfig.class;
		this.defaultResourceConfigBuilder = defaultResourceConfigBuilder;
	}

	/**
	 * Returns the set of service names attached to the profile whose identifier is
	 * passed as parameter
	 * 
	 * @param profile
	 *            the string profile name for which to return the set of attached
	 *            service names
	 * 
	 * @return the set of service names for the specified profile
	 */
	public List<String> getServices(String profile) {
		List<String> services = new ArrayList<String>();

		if (this.profiles != null) {
			String[] profiles = profile == null ? new String[] { ResourceConfig.ALL_PROFILES }
					: new String[] { ResourceConfig.ALL_PROFILES, profile };

			int index = 0;
			int length = profiles == null ? 0 : profiles.length;

			for (; index < length; index++) {
				List<String> tobeAdded = this.profiles.get(profiles[index]);
				if (tobeAdded == null)
					continue;
				services.addAll(tobeAdded);
			}
		}
		return Collections.unmodifiableList(services);
	}

	/**
	 * Returns the set of service names attached to the service provider whose
	 * identifier is passed as parameter
	 * 
	 * @param name
	 *            the string service provider name for which to return the set of
	 *            attached service names
	 * 
	 * @return the set of service names for the specified service provider
	 */
	public List<String> getFixedServices(String name) {
		List<String> fixedServices = null;
		if (this.fixed != null) {
			if ((fixedServices = this.fixed.get(name)) != null) {
				return fixedServices;
			}
		}
		return new ArrayList<String>();
	}

	/**
	 * The {@link AccessTreeImpl} applying on {@link ModelInstance}s configured by
	 * this {@link ModelConfiguration}
	 * 
	 * @return the applying {@link AccessTreeImpl}
	 */
	public MutableAccessTree<? extends MutableAccessNode> getAccessTree() {
		return this.accessTree;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#setDefaultResourceType(java.lang.Class)
	 */
	public ModelConfiguration setDefaultResourceType(Class<? extends Resource> defaultResourceType) {
		if (this.defaultResourceConfigBuilder != null) {
			this.defaultResourceConfigBuilder.setDefaultResourceType(defaultResourceType);
		}
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# setDefaultDataType(java.lang.Class)
	 */
	public ModelConfiguration setDefaultDataType(Class<?> defaultDataType) {
		if (this.defaultResourceConfigBuilder != null) {
			this.defaultResourceConfigBuilder.setDefaultDataType(defaultDataType);
		}
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# setDefaultModifiable(Modifiable)
	 */
	public ModelConfiguration setDefaultModifiable(Modifiable defaultModifiable) {
		if (this.defaultResourceConfigBuilder != null) {
			this.defaultResourceConfigBuilder.setDefaultModifiable(defaultModifiable);
		}
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      setDefaultUpdatePolicy(Resource.UpdatePolicy)
	 */
	public ModelConfiguration setDefaultUpdatePolicy(Resource.UpdatePolicy defaultUpdatePolicy) {
		if (this.defaultResourceConfigBuilder != null) {
			this.defaultResourceConfigBuilder.setDefaultUpdatePolicy(defaultUpdatePolicy);
		}
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      setProviderImplementationType(java.lang.Class)
	 */
	@Override
	public ModelConfiguration setProviderImplementationType(Class<? extends ServiceProviderImpl> serviceProviderType) {
		this.serviceProviderType = serviceProviderType;
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# getProviderImplementationType()
	 */
	@Override
	public Class<? extends ServiceProviderImpl> getProviderImplementationType() {
		if (this.serviceProviderType == null) {
			return this.defaultServiceProviderType;
		}
		return this.serviceProviderType;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      setServiceImplmentationType(java.lang.Class)
	 */
	@Override
	public ModelConfiguration setServiceImplmentationType(Class<? extends ServiceImpl> serviceType) {
		this.serviceType = serviceType;
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# getServiceImplementationType()
	 */
	@Override
	public Class<? extends ServiceImpl> getServiceImplementationType() {
		if (this.serviceType == null) {
			return this.defaultServiceType;
		}
		return this.serviceType;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      setResourceImplementationType(java.lang.Class)
	 */
	@Override
	public ModelConfiguration setResourceImplementationType(Class<? extends ResourceImpl> resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# getResourceImplementationType()
	 */
	@Override
	public Class<? extends ResourceImpl> getResourceImplementationType() {
		if (this.resourceType == null) {
			return this.defaultResourceType;
		}
		return this.resourceType;
	}
	
	/**
	 * Adds a {@link ResourceConfigCatalog} to the list of those of this
	 * ModelConfiguration
	 * 
	 * @param resourceConfigCatalog
	 *            the {@link ResourceConfigCatalog} to add
	 */
	public ModelConfiguration addResourceConfigCatalog(ResourceConfigCatalog resourceConfigCatalog) {
		if (resourceConfigCatalog != null) 
			this.resourceConfigCatalogs.add(resourceConfigCatalog);
		return this;
	}

	/**
	 * Creates and returns a new {@link ResourceDescriptor}
	 * 
	 * @return the newly created {@link ResourceDescriptor}
	 */
	public ResourceDescriptor getResourceDescriptor() {
		ResourceDescriptor descriptor = new ResourceDescriptor();
		descriptor.withResourceConfigType(this.getResourceConfigType());
		descriptor.withResourceImplementationType(this.getResourceImplementationType());
		return descriptor;
	}

	/**
	 * Retrieves and returns the previously registered {@link ResourceConfig}
	 * described by the {@link ResourceDescriptor} passed as parameter if it exists
	 * in this ModelConfiguration's {ResourceConfigCatalog}s
	 * 
	 * @param descriptor
	 *            the {@link ResourceDescriptor} describing the
	 *            {@link ResourceConfig} to be returned
	 * 
	 * @return the previously registered {@link ResourceConfig} described by the
	 *         specified {@link ResourceDescriptor} if it exists ; returns null
	 *         otherwise
	 */
	public ResourceConfig getResourceConfig(ResourceDescriptor descriptor) {
		return this.resourceConfigCatalogs.getResourceConfig(descriptor);
	}

	/**
	 * Creates and returns the {@link ResourceConfig} described by the
	 * {@link ResourceDescriptor} passed as parameter
	 * 
	 * @param descriptor
	 *            the {@link ResourceDescriptor} describing the
	 *            {@link ResourceConfig} to be returned
	 * 
	 * @return the {@link ResourceConfig} described by the specified
	 *         {@link ResourceDescriptor}
	 */
	public ResourceConfig createResourceConfig(ResourceDescriptor descriptor) {
		ResourceConfig resourceConfig = this.defaultResourceConfigBuilder.getResourceConfig(descriptor);
		return resourceConfig;
	}

	/**
	 * Returns the List of {@link ResourceConfig}s for the service whose name is
	 * passed as parameter
	 * 
	 * @return the list of {@link ResourceConfig}s for the specified service
	 */
	public List<ResourceConfig> getResourceConfigs(String serviceId) {
		return this.getResourceConfigs(ResourceConfig.ALL_PROFILES, serviceId);
	}

	/**
	 * Returns the List of {@link ResourceConfig}s for the service whose name is
	 * passed as parameter
	 * 
	 * @return the list of {@link ResourceConfig}s for the specified service
	 */
	public List<ResourceConfig> getResourceConfigs(String profile, String serviceId) {
		List<ResourceConfig> configs = new ArrayList<ResourceConfig>();

		Iterator<ResourceConfigCatalog> iterator = this.resourceConfigCatalogs.iterator();

		while (iterator.hasNext()) {
			configs.addAll(iterator.next().getResourceConfigs(profile, serviceId));
		}
		return configs;
	}

	/**
	 * Returns the ResourceConfig of the default Resource of the service whose name
	 * is passed as parameter if it has been defined
	 * 
	 * @param serviceName
	 *            the name of the service for which to retrieve the default
	 *            {@link ResourceConfig}
	 * 
	 * @return the default {@link ResourceConfig} for the specified service
	 */
	public ResourceConfig getDefaultResourceConfig(String profile, String serviceName) {
		ResourceConfig config = null;

		Iterator<ResourceConfigCatalog> iterator = this.resourceConfigCatalogs.iterator();

		while (iterator.hasNext()) {
			config = iterator.next().getDefaultResourceConfig(profile, serviceName);
			if (config != null)
				break;
		}
		return config;
	}

	/**
	 * Returns the ResourceConfig of the default Resource of the service whose name
	 * is passed as parameter if it has been defined
	 * 
	 * @param serviceName
	 *            the name of the service for which to retrieve the default
	 *            {@link ResourceConfig}
	 * 
	 * @return the default {@link ResourceConfig} for the specified service
	 */
	public ResourceConfig getDefaultResourceConfig(String serviceName) {
		return this.getDefaultResourceConfig(ResourceConfig.ALL_PROFILES, serviceName);
	}

	/**
	 * Defines the {@link ResourceConfig} type provided by this ModelConfiguration
	 * 
	 * @param resourceConfigType
	 *            this ModelConfiguration's {@link ResourceConfig} type
	 */
	public void setResourceConfigType(Class<? extends ResourceConfig> resourceConfigType) {
		this.resourceConfigType = resourceConfigType;
	}

	/**
	 * Returns the {@link ResourceConfig} type provided by this ModelConfiguration
	 * 
	 * @return this ModelConfiguration's {@link ResourceConfig} type
	 */
	public Class<? extends ResourceConfig> getResourceConfigType() {
		return this.resourceConfigType;
	}

	/**
	 * @param modelInstanceType
	 */
	public <C extends ModelConfiguration,I extends ModelInstance<C>> void setModelInstanceType(
		Class<I> modelInstanceType){
		this.modelInstanceType = modelInstanceType;
	}

	/**
	 * 
	 * @return
	 */
	public <C extends ModelConfiguration,I extends ModelInstance<C>> Class<I> 
	getModelInstanceType(){
		return (Class<I>) this.modelInstanceType;
	}
	
	/**
	 * Returns the list of String paths, starting from the service definition, of
	 * the attributes to be observed by the {@link ModelInstanceRegistration} of a
	 * {@link ModelInstance} configured by this ModelConfiguration
	 * 
	 * @return the list of String paths of the observed attributes
	 */
	public List<String> getObserved() {
		if (this.observed != null) {
			return Collections.unmodifiableList(observed);
		}
		return Collections.emptyList();
	}

	/**
	 * Attaches the specified list of String paths, starting from the service
	 * definition, of the attributes to be observed by the
	 * {@link ModelInstanceRegistration} of a {@link ModelInstance} configured by
	 * this ModelConfiguration
	 * 
	 * @param observed
	 *            the list of String paths of attributes to be observed
	 */
	public void setObserved(List<String> observed) {
		if (observed != null) {
			this.observed = Collections.unmodifiableList(observed);
		} else {
			this.observed = Collections.emptyList();
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      setResourceBuildPolicy(SensiNactResourceModelConfiguration.BuildPolicy)
	 */
	@Override
	public ModelConfiguration setResourceBuildPolicy(byte buildPolicy) {
		this.resourceBuildPolicy = buildPolicy;
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# getResourceBuildPolicy()
	 */
	@Override
	public byte getResourceBuildPolicy() {
		return this.resourceBuildPolicy;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      setServiceBuildPolicy(SensiNactResourceModelConfiguration.BuildPolicy)
	 */
	@Override
	public ModelConfiguration setServiceBuildPolicy(byte buildPolicy) {
		this.serviceBuildPolicy = buildPolicy;
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# getServiceBuildPolicy()
	 */
	@Override
	public byte getServiceBuildPolicy() {
		return this.serviceBuildPolicy;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration# getStartAtInitializationTime()
	 */
	@Override
	public boolean getStartAtInitializationTime() {
		return this.startAtInitializationTime;
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
	@Override
	public ModelConfiguration setStartAtInitializationTime(boolean startAtInitializationTime) {
		this.startAtInitializationTime = startAtInitializationTime;
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      getAccessibleMethods(java.lang.String, AccessLevelOption)
	 */
	@Override
	public List<MethodAccessibility> getAccessibleMethods(String path, AccessLevelOption accessLevelOption) {
		List<MethodAccessibility> list = this.accessTree.getRoot().get(path).getAccessibleMethods(accessLevelOption);
		return list;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelConfiguration#
	 *      getUserAccessLevelOption(java.lang.String, java.lang.String)
	 */
	@Override
	public AccessLevelOption getAuthenticatedAccessLevelOption(final String path, final String publicKey) {
		return AccessController.<AccessLevelOption>doPrivileged(new PrivilegedAction<AccessLevelOption>() {
			@Override
			public AccessLevelOption run() {
				return mediator.callService(AuthorizationService.class,
						new Executable<AuthorizationService, AccessLevelOption>() {
							@Override
							public AccessLevelOption execute(AuthorizationService service) throws Exception {
								return service.getAuthenticatedAccessLevelOption(path, publicKey);
							}
						});
			}
		});

	}
}

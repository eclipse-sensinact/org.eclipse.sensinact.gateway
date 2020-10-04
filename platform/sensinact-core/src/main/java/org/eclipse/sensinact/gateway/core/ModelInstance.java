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
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;
import org.eclipse.sensinact.gateway.core.ServiceProvider.LifecycleStatus;
import org.eclipse.sensinact.gateway.core.message.*;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessNodeImpl;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * A sensiNact Resource Model instance
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ModelInstance<C extends ModelConfiguration> implements SensiNactResourceModel<C>, LifecycleStatusListener {

	/**
	 * Returns the initial location of the sensiNact gateway and so of the service
	 * providers for which it is not specified. This method should be called once at
	 * initialization time
	 * 
	 * @return the initial string location value ( latitude:longitude)
	 */
	public static String defaultLocation(Mediator mediator) {
		double systemLatitude = 0d;
		double systemLongitude = 0d;
		try {
			systemLatitude = Double.parseDouble(mediator.getContext().getProperty(ServiceProvider.LATITUDE_PROPERTY));

			systemLongitude = Double.parseDouble(mediator.getContext().getProperty(ServiceProvider.LONGITUDE_PROPERTY));

		} catch (Exception e) {
			systemLatitude = ServiceProvider.DEFAULT_Kentyou_LOCATION_LATITUDE;
			systemLongitude = ServiceProvider.DEFAULT_Kentyou_LOCATION_LONGITUDE;
		}
		String locationStr = new StringBuilder().append(systemLatitude).append(":").append(systemLongitude).toString();
		return locationStr;
	}

	/**
	 * the {@link Mediator} allowing to interact with the OSGi host environment
	 */
	protected Mediator mediator;

	/**
	 * the {@link SensiNactResourceModelConfiguration} of this ModelInstance
	 */
	protected final C configuration;

	/**
	 * the root {@link ServiceProviderImpl} of this instance of the sensiNact
	 * resource model
	 */
	protected ServiceProviderImpl provider;

	/**
	 * <ul>
	 * <li>true if this SensiNactResourceModel has been registered in the OSGi host
	 * environment</li>
	 * <li>false otherwise</li>
	 * </ul>
	 */
	protected boolean registered;

	/**
	 * the String unique identifier of this SensiNactResourceModel in the OSGi host
	 * environment
	 */
	private final String identifier;

	/**
	 * Remote ID of the sensiNact instance
	 */
	private String namespace;

	/**
	 * the {@link MesssageHandler} handling messages coming from this
	 * SensiNactResourceModel
	 */
	protected MessageRouter messageRouter;

	/**
	 * the String identifier of the profile of this SnaServiceProvider
	 */
	protected final String profileId;

	/**
	 * The {@link ServiceRegistration} in the OSGi host environment for this
	 * {@link SensiNactResourceModel} instance
	 */
	private ModelInstanceRegistration registration;

	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * @param configuration
	 *            the extended {@link ModelConfiguration} gathering the
	 *            configuration properties applying on the ModelInstance to be
	 *            created
	 * 
	 * @throws InvalidServiceProviderException
	 */
	public ModelInstance(final Mediator mediator, C modelConfiguration, String name, String profileId)
			throws InvalidServiceProviderException {
		this.mediator = mediator;
		this.profileId = profileId;
		this.configuration = modelConfiguration;

		List<String> initialSetOfServices = this.configuration.getFixedServices(name);
		initialSetOfServices.addAll(this.configuration.getServices(profileId));

		this.provider = ReflectUtils.getInstance(ServiceProviderImpl.class,
				this.configuration.getProviderImplementationType(), new Object[] { this, name, initialSetOfServices });

		if (this.provider == null) {
			throw new InvalidServiceProviderException("Unable to instantiate the root services provider");
		}
		// retrieve the unique identifier if it exits
		this.identifier = this.mediator.callService(BundleValidation.class, new Executable<BundleValidation, String>() {
			@Override
			public String execute(BundleValidation service) throws Exception {
				return service.check(mediator.getContext().getBundle());
			}
		});
		this.namespace = this.mediator.callService(Core.class, new Executable<Core, String>() {
			@Override
			public String execute(Core core) throws Exception {
				return core.namespace();
			}
		});
	}


	/**
	 * @inheritDoc
	 * 
	 * @see SensiNactResourceModel#configuration()
	 */
	@Override
	public C configuration() {
		return this.configuration;
	}

	/**
	 * Returns a new {@link ServiceBuilder} instance
	 * 
	 * @return a new {@link ServiceBuilder} instance
	 */
	public ServiceBuilder getServiceBuilder() {
		ServiceBuilder builder = new ServiceBuilder(this.mediator, ServiceImpl.class);
		builder.configureImplementationClass(this.configuration.getServiceImplementationType());
		return builder;
	}

	/**
	 * Returns a new {@link ResourceBuilder} parameterized by the
	 * {@link ResourceConfig} passed as parameter
	 * 
	 * @param resourceConfig
	 *            the {@link ResourceConfig} parameterizing the
	 *            {@link ResourceBuilder} to be created
	 * 
	 * @return a new {@link ResourceBuilder} instance parameterized by the specified
	 *         {@link ResourceConfig}
	 */
	public ResourceBuilder getResourceBuilder(ResourceConfig resourceConfig) {
		ResourceBuilder builder = new ResourceBuilder(this.mediator, resourceConfig);
		return builder;
	}

	/**
	 * 
	 * @param descriptor
	 * @param buildPolicy
	 * 
	 * @return the appropriate {@link ResourceBuiler} according to the specified
	 *         {@link ResourceDescriptor} and the build policy
	 */
	protected ResourceBuilder getResourceBuilder(ResourceDescriptor descriptor, byte buildPolicy) {
		ResourceBuilder builder = null;

		if (SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
				SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION)) {
			builder = getResourceBuilder(descriptor);
		}
		if (builder == null && SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
				SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED)) {
			builder = createResourceBuilder(descriptor);
		}
		return builder;
	}

	/**
	 * Returns a {@link ResourceBuilder} wrapping a previously registered
	 * {@link ResourceConfig} for the {@link ResourceDescriptor} passed as
	 * parameter. If no appropriate {@link ResourceConfig} can be retrieved, null is
	 * returned
	 * 
	 * @param descriptor
	 *            the {@link ResourceDescriptor} describing the
	 *            {@link ResourceConfig} wrapped by the {@link ResourceBuilder} to
	 *            be returned
	 * 
	 * @return a new {@link ResourceBuilder} instance
	 */
	public ResourceBuilder getResourceBuilder(ResourceDescriptor descriptor) {
		ResourceConfig resourceConfig = this.configuration().getResourceConfig(descriptor);

		if (resourceConfig == null) {
			return null;
		}
		return configureResourceBuilder(resourceConfig, descriptor);
	}

	/**
	 * Returns a {@link ResourceBuilder} wrapping a newly created
	 * {@link ResourceConfig} for the {@link ResourceDescriptor} passed as
	 * parameter. If no appropriate {@link ResourceConfig} can be created, null is
	 * returned
	 * 
	 * @param descriptor
	 *            the {@link ResourceDescriptor} describing the
	 *            {@link ResourceConfig} wrapped by the {@link ResourceBuilder} to
	 *            be returned
	 * 
	 * @return a new {@link ResourceBuilder} instance
	 */
	public ResourceBuilder createResourceBuilder(ResourceDescriptor descriptor) {
		ResourceConfig resourceConfig = this.configuration().createResourceConfig(descriptor);

		if (resourceConfig == null) {
			return null;
		}
		return configureResourceBuilder(resourceConfig, descriptor);
	}

	/**
	 * Returns a {@link ResourceBuilder} capable of creating a new resource based on
	 * both the {@link ResourceConfig} and the {@link ResourceDescriptor} passed as
	 * parameters
	 * 
	 * @param resourceConfig
	 *            the {@link ResourceConfig} that will be used by the
	 *            {@link ResourceBuilder} to be returned
	 * @param descriptor
	 *            the {@link ResourceDescriptor} describing the
	 *            {@link ResourceConfig} wrapped by the {@link ResourceBuilder} to
	 *            be returned
	 * 
	 * @return a new {@link ResourceBuilder} instance
	 */
	private <G extends ResourceConfig> ResourceBuilder configureResourceBuilder(G resourceConfig,
			ResourceDescriptor descriptor) {
		ResourceBuilder builder = new ResourceBuilder(this.mediator, resourceConfig);

		if (descriptor.resourceName() != null) {
			builder.configureName(descriptor.resourceName());
		}
		if (descriptor.dataType() != null) {
			builder.configureType(descriptor.dataType());
		}
		if (descriptor.dataValue() != null) {
			builder.configureValue(descriptor.dataValue());
		}
		if (descriptor.modifiable() != null) {
			builder.configureRequirement(DataResource.VALUE, AttributeBuilder.Requirement.MODIFIABLE,
					descriptor.modifiable());
		}
		if (descriptor.hidden() != null) {
			builder.configureRequirement(DataResource.VALUE, AttributeBuilder.Requirement.HIDDEN,
					descriptor.hidden().booleanValue());
		}
		return builder;
	}

	/**
	 * Returns the {@link Mediator} of this SensiNactResourceModel allowing to
	 * interact with the OSGi host environment
	 * 
	 * @return this SensiNactResourceModel's {@link Mediator}
	 */
	public Mediator mediator() {
		return this.mediator;
	}

	/**
	 * Posts the {@link SnaMessage} past as parameter to the {@link MessageRouter}
	 * of this SensiNactResourceModel
	 * 
	 * @param message
	 *            the {@link SnaMessage} to post
	 */
	public void postMessage(SnaMessage<?> message) {
		if (this.messageRouter == null) {
			return;
		}
		((AbstractSnaMessage<?>)message).put("namespace", this.namespace, true);
		this.messageRouter.handle(message);
	}

	/**
	 * Registers this sensiNact resource model instance in the OSGi host
	 * environment.
	 * 
	 * @throws ModelAlreadyRegisteredException
	 *             if this sensiNact resource model instance is already registered
	 */
	protected final void register() throws ModelAlreadyRegisteredException {
		if (this.registered) {
			throw new ModelAlreadyRegisteredException(this.registration.getName());
		}
		final String name = this.getName();

		boolean exists = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {
			@Override
			public Boolean run() {
				Collection<ServiceReference<SensiNactResourceModel>> references = null;
				try {
					references = ModelInstance.this.mediator.getContext().getServiceReferences(
							SensiNactResourceModel.class,
							new StringBuilder().append("(name=").append(name).append(")").toString());
				} catch (InvalidSyntaxException e) {
					ModelInstance.this.mediator.error(e);
				}
				return (references != null && references.size() > 0);
			}
		});
		if (exists) {
			throw new ModelAlreadyRegisteredException(name);
		}
		final String uri = UriUtils.getUri(new String[] { name });

		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("name", name);

		String location = null;
		try {
			location = this.getRootElement().getLocation();
			props.put(LocationResource.LOCATION, location);

		} catch (NullPointerException e) {
			mediator.debug(String.format("No initial location defined for %s", name));
		}
		AccessNode node = null;
		AccessNode root = this.configuration.getAccessTree().getRoot();
		AccessMethod.Type[] accessMethodTypes = AccessMethod.Type.values();
		int typesLength = accessMethodTypes == null ? 0 : accessMethodTypes.length;
		if ((node = (AccessNodeImpl<?>) root.get(uri)) == null) {
			node = root;
		}
		int index = 0;
		for (; index < typesLength; index++) {
			AccessLevelOption accessLevelOption = node.getAccessLevelOption(accessMethodTypes[index]);
			props.put(new StringBuilder().append(name).append(".").append(accessMethodTypes[index].name()).toString(),
					accessLevelOption.getAccessLevel().getLevel());
		}
		ServiceRegistration<SensiNactResourceModel> instanceRegistration = 
			AccessController.<ServiceRegistration<SensiNactResourceModel>>doPrivileged(
				new PrivilegedAction<ServiceRegistration<SensiNactResourceModel>>() {
					@Override
					public ServiceRegistration<SensiNactResourceModel> run() {
						return ModelInstance.this.mediator.getContext(
							).registerService(SensiNactResourceModel.class, ModelInstance.this, props);
					}
				});
		if (instanceRegistration != null) {
			this.registered = true;
			List<String> observed = this.configuration.getObserved();

			this.registration = new ModelInstanceRegistration(uri, observed, instanceRegistration, this.configuration);
			this.messageRouter = new SnaMessageListener(mediator, this.configuration());
			
			boolean pattern = false;

			StringBuilder observedBuilder = new StringBuilder().append(uri);
			if (observed != null && !observed.isEmpty()) {
				observedBuilder.append("(/admin/location/value");
				Iterator<String> it = observed.iterator();
				while (it.hasNext()) {
					String obs = null;
					String[] uriElements = UriUtils.getUriElements(it.next());
					switch (uriElements.length) {
					case 0:
					case 1:
						continue;
					case 2:
						obs = UriUtils.getUri(uriElements).concat("/value");
						break;
					case 3:
						obs = UriUtils.getUri(uriElements);
						break;
					default:
						continue;
					}
					observedBuilder.append("|");
					observedBuilder.append(obs);
				}
				observedBuilder.append(")");
				pattern = true;
			} else {
				observedBuilder.append("/admin/location/value");
			}
			SnaFilter filter = new SnaFilter(mediator, observedBuilder.toString(), pattern, false);

			filter.addHandledType(SnaMessage.Type.UPDATE);
			this.messageRouter.addCallback(filter, registration);

			filter = new SnaFilter(mediator, "(/[^/]+)+", true, false);
			filter.addHandledType(SnaMessage.Type.LIFECYCLE);
			this.messageRouter.addCallback(filter, registration);

			if (this.configuration().getStartAtInitializationTime()) {
				this.provider.start();
			}
		}
	}

	/**
	 * Unregisters this sensiNact resource model instance from the OSGi host
	 * environment
	 * 
	 * @throws IllegalStateException
	 *             if this sensiNact resource model instance is not registered
	 */
	public final void unregister() throws IllegalStateException {
		if (!this.isRegistered()) {
			throw new IllegalStateException(this.registration.getName());
		}
		this.registered = false;
		try {
			this.getRootElement().stop();

		} catch (Exception e) {
			mediator.error(e);
		}
		this.messageRouter.close(true);
		this.messageRouter = null;

		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				ModelInstance.this.registration.unregister();
				return null;
			}
		});
	}

	/**
	 * Returns true if this SensiNactResourceModel has been registered; returns
	 * false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if this SensiNactResourceModel has already been
	 *         registered</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean isRegistered() {
		return this.registered;
	}

	/**
	 * Returns the root {@link ServiceProviderImpl} of this instance of the
	 * sensiNact resource model
	 * 
	 * @return this resource model instance's root {@link ServiceProviderImpl}
	 * 
	 * @see SensiNactResourceModel#getRootElement()
	 */
	@Override
	public ServiceProviderImpl getRootElement() {
		return this.provider;
	}

	/**
	 * @inheritedDoc
	 *
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.getRootElement().getName();
	}

	/**
	 * @inheritedDoc
	 *
	 * @see SensiNactResourceModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @param filter
	 * @param callback
	 */
	public void registerCallback(SnaFilter filter, MidCallback callback) {
		if (this.messageRouter == null) {
			return;
		}
		this.messageRouter.addCallback(filter, callback);
	}

	/**
	 * 
	 * @param callback
	 */
	public void unregisterCallback(String callback) {
		if (this.messageRouter == null) {
			return;
		}
		this.messageRouter.deleteCallback(callback);
	}

	/**
	 * Returns the string identifier of the profile to which this model instance
	 * belongs to
	 * 
	 * @return this model instance profile identifier
	 */
	public String getProfile() {
		return this.profileId;
	}

	/**
	 * @inheritDoc
	 *
	 * @see LifecycleStatusListener# update(ServiceProvider.LifecycleStatus)
	 */
	@Override
	public void update(LifecycleStatus status) {
		this.registration.updateLifecycle(status);
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModel#getProperties()
	 */
	@Override
	public Dictionary<String, String> getProperties() {
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("uri", UriUtils.getUri(new String[] { this.getName() }));
		props.put("lifecycle.status", this.getRootElement().getStatus().name());
		return props;
	}

	/**
	 * Returns the set of the specified {@link ModelElement} accessible
	 * {@link AccessMethod.Type}s for the {@link AccessLevelOption} passed as
	 * parameter and
	 * 
	 * @param modelElement
	 *            the {@link ModelElement} for which to retrieve the set of
	 *            accessible {@link AccessMethod.Type}s
	 * @param accessLevelOption
	 *            the requirer {@link AccessLevelOption}
	 * 
	 * @return the set of accessible {@link AccessMethod.Type} of the specified
	 *         {@link ModelElement} for the specified {@link AccessLevelOption}
	 */
	public <I extends ModelInstance<?>, M extends ModelElementProxy, P extends ProcessableData, E extends Nameable, R extends Nameable> List<MethodAccessibility> getAuthorizations(
			ModelElement<I, M, P, E, R> modelElement, AccessLevelOption accessLevelOption) {
		if (modelElement.getModelInstance() != this) {
			throw new RuntimeException("the model element argument must belong to this model instance");
		}
		final String path = modelElement.getPath();
		return this.configuration().getAccessibleMethods(path, accessLevelOption);
	}
}

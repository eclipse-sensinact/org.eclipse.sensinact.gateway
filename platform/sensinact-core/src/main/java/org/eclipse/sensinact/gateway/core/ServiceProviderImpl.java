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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Localizable;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;
import org.eclipse.sensinact.gateway.common.primitive.Stateful;
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;

/**
 * This class represents a ServiceProvider on the sensiNact gateway.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProviderImpl extends
		ModelElement<ModelInstance<?>, ServiceProviderProxy, ServiceProviderProcessableData<?>, ServiceImpl, Service>
		implements Localizable {
	class ServiceProviderProxyWrapper extends ModelElementProxyWrapper
			implements ServiceCollection, Localizable, Stateful<ServiceProvider.LifecycleStatus> {
		protected ServiceProviderProxyWrapper(ServiceProviderProxy proxy, ImmutableAccessTree tree) {
			super(proxy, tree);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.ServiceCollection# getServices()
		 */
		@Override
		public List<Service> getServices() {
			return super.list();
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.ServiceCollection#
		 *      getService(java.lang.String)
		 */
		@Override
		public Service getService(String service) {
			return super.element(service);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.common.primitive.Localizable#
		 *      getLocation()
		 */
		@Override
		public String getLocation() {
			String location = null;
			Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
			if (admin != null) {
				JSONObject response = admin.get(LocationResource.LOCATION).getResponse();
				location = String.valueOf(response.opt(DataResource.VALUE));
			}
			return location;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.common.primitive.Localizable#
		 *      setLocation(java.lang.String)
		 */
		@Override
		public String setLocation(String location) throws InvalidValueException {
			String setLocation = null;
			Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
			if (admin != null) {
				JSONObject response = admin.set(LocationResource.LOCATION, location).getResponse();
				setLocation = String.valueOf(response.opt(DataResource.VALUE));
			}
			return setLocation;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.common.primitive.Stateful#getStatus()
		 */
		@Override
		public ServiceProvider.LifecycleStatus getStatus() {
			ServiceProvider.LifecycleStatus status = null;
			Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
			if (admin != null) {
				JSONObject response = admin.get(ServiceProvider.LIFECYCLE_STATUS).getResponse();
				status = ServiceProvider.LifecycleStatus.valueOf(String.valueOf(response.opt(DataResource.VALUE)));
			}
			return status;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.common.primitive.Stateful#
		 *      setStatus(java.lang.Enum)
		 */
		@Override
		public ServiceProvider.LifecycleStatus setStatus(ServiceProvider.LifecycleStatus status)
				throws InvalidValueException {
			ServiceProvider.LifecycleStatus setStatus = null;
			Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
			if (admin != null) {
				JSONObject response = admin.set(ServiceProvider.LIFECYCLE_STATUS, status).getResponse();
				setStatus = ServiceProvider.LifecycleStatus.valueOf(String.valueOf(response.opt(DataResource.VALUE)));
			}
			return setStatus;
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#isAccessible()
		 */
		@Override
		public boolean isAccessible() {
			return true;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.common.primitive.Describable#getDescription()
		 */
		public Description getDescription() {
			return new Description() {
				@Override
				public String getName() {
					return ServiceProviderImpl.this.getName();
				}

				@Override
				public String getJSON() {
					StringBuilder buffer = new StringBuilder();
					buffer.append(JSONUtils.OPEN_BRACE);
					buffer.append(JSONUtils.QUOTE);
					buffer.append("name");
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COLON);
					buffer.append(JSONUtils.QUOTE);
					buffer.append(this.getName());
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COMMA);
					buffer.append(JSONUtils.QUOTE);
					buffer.append("services");
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COLON);
					buffer.append(JSONUtils.OPEN_BRACKET);
					int index = 0;

					Enumeration<Service> enumeration = ServiceProviderProxyWrapper.this.elements();

					while (enumeration.hasMoreElements()) {
						buffer.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
						buffer.append(JSONUtils.QUOTE);
						buffer.append(enumeration.nextElement().getName());
						buffer.append(JSONUtils.QUOTE);
						index++;
					}
					buffer.append(JSONUtils.CLOSE_BRACKET);
					buffer.append(JSONUtils.CLOSE_BRACE);
					return buffer.toString();
				}

				@Override
				public String getJSONDescription() {
					StringBuilder buffer = new StringBuilder();
					buffer.append(JSONUtils.OPEN_BRACE);
					buffer.append(JSONUtils.QUOTE);
					buffer.append("name");
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COLON);
					buffer.append(JSONUtils.QUOTE);
					buffer.append(this.getName());
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COMMA);
					buffer.append(JSONUtils.QUOTE);
					buffer.append("services");
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COLON);
					buffer.append(JSONUtils.OPEN_BRACKET);
					int index = 0;

					Enumeration<Service> enumeration = ServiceProviderProxyWrapper.this.elements();

					while (enumeration.hasMoreElements()) {
						buffer.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
						buffer.append(JSONUtils.QUOTE);
						buffer.append(enumeration.nextElement().getDescription().getJSONDescription());
						buffer.append(JSONUtils.QUOTE);
						index++;
					}
					buffer.append(JSONUtils.CLOSE_BRACKET);
					buffer.append(JSONUtils.CLOSE_BRACE);
					return buffer.toString();
				}
			};
		}
	}

	/**
	 * the list of listeners of this ServiceProviderImpl's LifecycleStatus
	 */
	protected List<LifecycleStatusListener> listeners;

	/**
	 * the list of pre-defined service names
	 */
	protected List<String> serviceNames;

	/**
	 * Constructor
	 * 
	 * @throws InvalidServiceException
	 * @throws InvalidServiceProviderException
	 */
	public ServiceProviderImpl(ModelInstance<?> modelInstance, String name) throws InvalidServiceProviderException {
		this(modelInstance, name, Collections.<String>emptyList());
	}

	/**
	 * Constructor
	 * 
	 * @throws InvalidServiceException
	 * @throws InvalidServiceProviderException
	 */
	public ServiceProviderImpl(ModelInstance<?> modelInstance, String name, List<String> serviceNames)
			throws InvalidServiceProviderException {
		super(modelInstance, null, UriUtils.getUri(new String[] { name }));
		this.serviceNames = new ArrayList<String>(serviceNames);
		try {
			this.createAdministrationService();

		} catch (Exception e) {
			throw new InvalidServiceProviderException(e);
		}
		this.listeners = new ArrayList<LifecycleStatusListener>();
		this.listeners.add(super.modelInstance);
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement# process(ProcessableData)
	 */
	@Override
	public void process(ServiceProviderProcessableData<?> data) {
		if (data == null) {
			return;
		}
		Iterator<ServiceProcessableData<?>> iterator = (Iterator<ServiceProcessableData<?>>) data.iterator();

		while (iterator.hasNext()) {
			ServiceProcessableData<?> serviceProcessableData = iterator.next();
			String serviceId = serviceProcessableData.getServiceId();

			if (serviceId == null) {
				continue;
			}
			ServiceImpl service = super.element(serviceId);
			if (service == null) {
				try {
					service = this.addService(serviceId);

				} catch (Exception e) {
					super.modelInstance.mediator().error(e);
				}
			}
			if (service == null) {
				super.modelInstance.mediator().warn("Service '%s' not found", serviceId);
				continue;
			}
			service.process(serviceProcessableData);
		}
	}

	/**
	 * Returns this service provider's administration service
	 * 
	 * @return this service provider's administration service
	 */
	public ServiceImpl getAdminService() {
		return this.getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
	}

	/**
	 * Returns the {@link ServiceImpl} provided by this ServiceProvider whose name
	 * is passed as parameter
	 * 
	 * @param service
	 *            the name of the {@link ServiceImpl}
	 * @return the {@link ServiceImpl} with the specified name
	 */
	public ServiceImpl getService(String service) {
		return super.element(service);
	}

	/**
	 * Returns the list {@link ServiceImpl}s provided by this ServiceProvider
	 * 
	 * @return the list of {@link ServiceImpl}s of this ServiceProvider
	 */
	public List<ServiceImpl> getServices() {
		synchronized (super.elements) {
			return Collections.<ServiceImpl>unmodifiableList(super.elements);
		}
	}

	/**
	 * Returns the string value of the location resource of this service provider's
	 * administration service
	 * 
	 * @return the string value of the location resource
	 */
	public String getLocation() {
		return (String) this.getAdminService().getResource(LocationResource.LOCATION).getAttribute(DataResource.VALUE)
				.getValue();
	}

	/**
	 * Sets the string value of the location resource of the administration service
	 * of this service provider
	 * 
	 * @return the set string value of the location resource of the administration
	 *         service
	 * 
	 * @throws InvalidValueException
	 */
	public String setLocation(String location) throws InvalidValueException {
		return (String) this.getAdminService().getResource(LocationResource.LOCATION).getAttribute(DataResource.VALUE)
				.setValue(location);
	}

	/**
	 * Gets the status of this sensiNact service provider.
	 * 
	 * @return this sensiNact service provider status
	 */
	public ServiceProvider.LifecycleStatus getStatus() {
		return (ServiceProvider.LifecycleStatus) this.getAdminService().getResource(ServiceProvider.LIFECYCLE_STATUS)
				.getAttribute(DataResource.VALUE).getValue();
	}

	/**
	 * Sets the status of this sensiNact service provider.
	 * 
	 * @param status
	 *            this sensiNact service provider status
	 */
	public ServiceProvider.LifecycleStatus setStatus(ServiceProvider.LifecycleStatus status) {
		Attribute attribute = this.getAdminService().getResource(ServiceProvider.LIFECYCLE_STATUS)
				.getAttribute(DataResource.VALUE);

		if (status != null) {
			try {
				ServiceProvider.LifecycleStatus newStatus = (ServiceProvider.LifecycleStatus) attribute
						.setValue(status);

				int index = 0;
				int length = this.listeners == null ? 0 : this.listeners.size();

				for (; index < length; index++) {
					this.listeners.get(index).update(newStatus);
				}
			} catch (InvalidValueException e) {
				super.modelInstance.mediator().error(e);
			}
		}
		return (ServiceProvider.LifecycleStatus) attribute.getValue();
	}

	/**
	 * Creates and returns the administration service of this ServiceProviderImpl
	 * 
	 * @return the new created {@link ServiceImpl} administration service instance
	 * @throws InvalidValueException
	 * @throws InvalidServiceException
	 * @throws InvalidResourceException
	 */
	protected ServiceImpl createAdministrationService()
			throws InvalidServiceException, InvalidResourceException, InvalidValueException {
		ServiceBuilder builder = super.modelInstance.getServiceBuilder();
		builder.configureName(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
		ServiceImpl admin = this.addService(builder);

		byte buildPolicy = (byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy()
				| SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());

		List<ResourceConfig> resourceConfigs = super.getModelInstance().configuration()
				.getResourceConfigs(ServiceProvider.ADMINISTRATION_SERVICE_NAME);

		int index = -1;

		if (admin.getResource(ServiceProvider.LIFECYCLE_STATUS) == null) {
			ResourceBuilder statusResourceBuilder = super.getModelInstance()
					.getResourceBuilder(super.getModelInstance().configuration().getResourceDescriptor()
							.withResourceType(PropertyResource.class).withResourceName(ServiceProvider.LIFECYCLE_STATUS)
							.withDataType(ServiceProvider.LifecycleStatus.class)
							.withDataValue(ServiceProvider.LifecycleStatus.INACTIVE).withHidden(true)
							.withUpdatePolicy(UpdatePolicy.AUTO).withModifiable(Modifiable.UPDATABLE), buildPolicy);
			admin.addResource(statusResourceBuilder);
		}
		if (admin.getResource(ServiceProvider.FRIENDLY_NAME) == null) {
			ResourceBuilder friendlyNameResourceBuilder = null;
			ResourceConfig rc = null;
			if ((index = resourceConfigs.indexOf(new Name<ResourceConfig>(ServiceProvider.FRIENDLY_NAME, true))) > -1) {
				rc = resourceConfigs.get(index);
				friendlyNameResourceBuilder = super.getModelInstance().getResourceBuilder(rc);
				index = -1;

			} else {
				friendlyNameResourceBuilder = super.getModelInstance().getResourceBuilder(super.getModelInstance()
						.configuration().getResourceDescriptor().withResourceType(PropertyResource.class)
						.withResourceName(ServiceProvider.FRIENDLY_NAME).withDataType(String.class).withHidden(false)
						.withUpdatePolicy(UpdatePolicy.AUTO).withModifiable(Modifiable.MODIFIABLE), buildPolicy);
			}
			friendlyNameResourceBuilder.configureName(ServiceProvider.FRIENDLY_NAME);
			admin.addResource(friendlyNameResourceBuilder);
		}
		if (admin.getResource(LocationResource.LOCATION) == null) {
			ResourceBuilder locationResourceBuilder = null;
			ResourceConfig rc = null;
			if ((index = resourceConfigs.indexOf(new Name<ResourceConfig>(LocationResource.LOCATION, true))) > -1) {
				rc = resourceConfigs.get(index);
				locationResourceBuilder = super.getModelInstance().getResourceBuilder(rc);
				index = -1;
			} else {
				locationResourceBuilder = super.getModelInstance().getResourceBuilder(super.getModelInstance()
						.configuration().getResourceDescriptor().withResourceType(LocationResource.class)
						.withResourceName(LocationResource.LOCATION).withDataType(String.class)
						.withDataValue(ModelInstance.defaultLocation(super.modelInstance.mediator())).withHidden(false)
						.withModifiable(Modifiable.MODIFIABLE), buildPolicy);
			}
			locationResourceBuilder.configureName(LocationResource.LOCATION);
			admin.addResource(locationResourceBuilder);
		}
		if (admin.getResource(ServiceProvider.BRIDGE) == null) {
			ResourceBuilder bridgeResourceBuilder = null;
			try {
				bridgeResourceBuilder = super.getModelInstance()
						.getResourceBuilder(
								super.getModelInstance().configuration().getResourceDescriptor()
										.withResourceType(PropertyResource.class)
										.withResourceName(ServiceProvider.BRIDGE).withDataType(String.class)
										.withDataValue(super.modelInstance.mediator().getContext().getBundle()
												.getSymbolicName())
										.withHidden(false).withUpdatePolicy(UpdatePolicy.AUTO)
										.withModifiable(Modifiable.FIXED),
								buildPolicy);

				admin.addResource(bridgeResourceBuilder);

			} catch (Exception e) {
				super.modelInstance.mediator().debug("Unable to create the 'bridge' resource");
				bridgeResourceBuilder = null;
			}
		}
		if (admin.getResource(ServiceProvider.ICON) == null) {
			ResourceBuilder iconResourceBuilder = null;
			ResourceConfig rc = null;
			if ((index = resourceConfigs.indexOf(new Name<ResourceConfig>(ServiceProvider.ICON, true))) > -1) {
				rc = resourceConfigs.get(index);
				iconResourceBuilder = super.getModelInstance().getResourceBuilder(rc);
				index = -1;
			} else {
				iconResourceBuilder = super.getModelInstance().getResourceBuilder(super.getModelInstance()
						.configuration().getResourceDescriptor().withResourceType(PropertyResource.class)
						.withResourceName(ServiceProvider.ICON).withDataType(String.class).withHidden(false)
						.withUpdatePolicy(UpdatePolicy.AUTO).withModifiable(Modifiable.MODIFIABLE), buildPolicy);
			}
			iconResourceBuilder.configureName(ServiceProvider.ICON);
			admin.addResource(iconResourceBuilder);
		}
		return admin;
	}

	/**
	 * Creates and returns a new {@link ServiceImpl} instance whose name is passed
	 * as parameter
	 * 
	 * @param string
	 *            the name of the {@link ServiceImpl} to instantiate
	 * @return the new created {@link ServiceImpl} instance
	 * @throws InvalidValueException
	 * @throws InvalidServiceException
	 * @throws InvalidResourceException
	 */
	public ServiceImpl addService(String serviceName)
			throws InvalidServiceException, InvalidResourceException, InvalidValueException {
		ServiceImpl service = null;

		if ((service = this.getService(serviceName)) != null) {
			super.modelInstance.mediator().warn("'%s' service already exists", serviceName);

			return service;
		}
		byte buildPolicy = getModelInstance().configuration().getServiceBuildPolicy();

		if (!SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
				SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED)
				&& !this.serviceNames.contains(serviceName)) {
			super.modelInstance.mediator().warn("Incompatible build policy : unable to create the '%s' service",
					serviceName);

			return null;
		}
		ServiceBuilder builder = super.modelInstance.getServiceBuilder();
		builder.configureName(serviceName);
		try {
			service = this.addService(builder);

		} catch (Exception e) {
			throw new InvalidServiceException(e);
		}
		buildPolicy = getModelInstance().configuration().getResourceBuildPolicy();

		if (service != null && SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
				SensiNactResourceModelConfiguration.BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION)) {
			List<ResourceConfig> resources = super.modelInstance.configuration()
					.getResourceConfigs(super.modelInstance.getProfile(), serviceName);

			if (super.modelInstance.getProfile() != null
					&& !ResourceConfig.ALL_PROFILES.equals(super.modelInstance.getProfile())) {
				resources.addAll(super.modelInstance.configuration().getResourceConfigs(ResourceConfig.ALL_PROFILES,
						serviceName));
			}
			ResourceConfig defaultResourceConfig = super.modelInstance.configuration()
					.getDefaultResourceConfig(serviceName);

			if (defaultResourceConfig != null) {
				defaultResourceConfig.configureName(serviceName, serviceName);
			}
			if (resources == null) {
				resources = Collections.emptyList();
			}
			super.modelInstance.mediator().info("New service discovered for '%s' service provider : %s", super.name,
					serviceName);

			Iterator<ResourceConfig> iterator = resources.iterator();
			while (iterator.hasNext()) {
				ResourceConfig resourceConfig = iterator.next();
				ResourceBuilder resourceBuilder = super.modelInstance.getResourceBuilder(resourceConfig);

				if (resourceConfig != defaultResourceConfig) {
					resourceConfig.configureName(serviceName, resourceConfig.getName().toLowerCase());
				}
				try {
					service.addResource(resourceBuilder);
				} catch (InvalidResourceException e) {
					throw new InvalidServiceException(e);
				}
			}
		}
		return service;
	}

	/**
	 * Creates and returns a new {@link ServiceImpl} instance whose name is passed
	 * as parameter
	 * 
	 * @param string
	 *            the name of the {@link ServiceImpl} to instantiate
	 * @return the new created {@link ServiceImpl} instance
	 * @throws InvalidValueException
	 * @throws InvalidServiceException
	 * @throws InvalidResourceException
	 */
	protected ServiceImpl addService(ServiceBuilder builder)
			throws InvalidServiceException, InvalidResourceException, InvalidValueException {
		ServiceImpl service = builder.build(super.modelInstance, this);
		if (service == null) {
			super.getModelInstance().mediator().debug("Unable to create the service '%s'", builder.getConfiguredName());
			return null;
		}
		return super.addElement(service) ? service : null;
	}

	/**
	 * @param serviceName
	 * @return
	 */
	public boolean removeService(String serviceName) {
		ServiceImpl service = super.removeElement(serviceName);
		if (service != null) {
			service.stop();
			return true;
		}
		return false;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getProxyType()
	 */
	protected Class<? extends ElementsProxy<Service>> getProxyType() {
		return ServiceProvider.class;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#start()
	 */
	@Override
	public void start() {
		if (!super.getModelInstance().isRegistered()) {
			this.modelInstance.mediator().error("The resource model is not registered");
			return;
		}
		this.setStatus(ServiceProvider.LifecycleStatus.JOINING);
		try {
			super.start();
			this.doStart();

		} catch (Exception e) {
			super.modelInstance.mediator().error(e);
			this.stop();
			return;
		}
		this.setStatus(ServiceProvider.LifecycleStatus.ACTIVE);
		super.modelInstance.mediator().debug("ServiceProvider '%s' started", this.getName());
	}

	/**
	 * Method that does nothing by default, to be overwritten to complete this
	 * ServiceProviderImpl starting process.
	 */
	protected void doStart() throws Exception {
		byte buildPolicy = super.modelInstance.configuration().getServiceBuildPolicy();

		if (SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
				SensiNactResourceModelConfiguration.BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION)) {
			int index = 0;
			int length = this.serviceNames == null ? 0 : serviceNames.size();

			for (; index < length; index++) {
				try {
					this.addService(this.serviceNames.get(index));

				} catch (Exception e) {
					throw new InvalidServiceProviderException(e);
				}
			}
		}
	}

	/**
	 * Stops this ServiceProvider and unregisters all its services
	 */
	public void stop() {
		this.setStatus(ServiceProvider.LifecycleStatus.LEAVING);
		try {
			super.stop();
			this.doStop();

		} catch (Exception e) {
			super.modelInstance.mediator().error(e);
		}
		this.setStatus(ServiceProvider.LifecycleStatus.INACTIVE);
	}

	/**
	 * Method that does nothing by default, to be overwritten to complete this
	 * ServiceProviderImpl starting process.
	 */
	protected void doStop() throws Exception {
		// to be overwritten
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getRegisteredEvent()
	 */
	@Override
	protected SnaLifecycleMessage.Lifecycle getRegisteredEvent() {
		return SnaLifecycleMessage.Lifecycle.PROVIDER_APPEARING;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getUnregisteredEvent()
	 */
	@Override
	protected SnaLifecycleMessage.Lifecycle getUnregisteredEvent() {
		return SnaLifecycleMessage.Lifecycle.PROVIDER_DISAPPEARING;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.SensiNactResourceModelElement#
	 *      getProxy(java.util.List)
	 */
	@Override
	public ServiceProviderProxy getProxy(List<MethodAccessibility> methodAccessibilities) {
		return new ServiceProviderProxy(super.modelInstance.mediator(), super.getName());
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.ModelElement#
	 *      getElementProxy(org.eclipse.sensinact.gateway.core.security.AccessTree,
	 *      org.eclipse.sensinact.gateway.common.primitive.Nameable)
	 */
	@Override
	protected Service getElementProxy(AccessTree<?> tree, ServiceImpl element) throws ModelElementProxyBuildException {
		Service service = element.getProxy(tree);
		return service;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.ModelElement#
	 *      getWrapper(org.eclipse.sensinact.gateway.core.ModelElementProxy,
	 *      org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree)
	 */
	@Override
	protected ModelElementProxyWrapper getWrapper(ServiceProviderProxy proxy, ImmutableAccessTree tree) {
		return new ServiceProviderProxyWrapper(proxy, tree);
	}

}

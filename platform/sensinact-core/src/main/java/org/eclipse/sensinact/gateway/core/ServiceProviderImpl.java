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

import static org.eclipse.sensinact.gateway.common.primitive.Modifiable.FIXED;
import static org.eclipse.sensinact.gateway.common.primitive.Modifiable.MODIFIABLE;
import static org.eclipse.sensinact.gateway.common.primitive.Modifiable.UPDATABLE;
import static org.eclipse.sensinact.gateway.core.LocationResource.LOCATION;
import static org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy.AUTO;
import static org.eclipse.sensinact.gateway.core.ServiceProvider.BRIDGE;
import static org.eclipse.sensinact.gateway.core.ServiceProvider.FRIENDLY_NAME;
import static org.eclipse.sensinact.gateway.core.ServiceProvider.ICON;
import static org.eclipse.sensinact.gateway.core.ServiceProvider.LIFECYCLE_STATUS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Localizable;
import org.eclipse.sensinact.gateway.common.primitive.Stateful;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a ServiceProvider on the sensiNact gateway.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProviderImpl extends
		ModelElement<ModelInstance<?>, ServiceProviderProxy, ServiceProviderProcessableData<?>, ServiceImpl, Service>
		implements Localizable {
	private static final Logger LOG=LoggerFactory.getLogger(ServiceProviderImpl.class);

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
				JSONObject response = admin.get(LocationResource.LOCATION, DataResource.VALUE).getResponse();
				location = String.valueOf(response.opt(DataResource.VALUE));
			}
			return location;
		}

		@Override
		public String setLocation(String location) throws InvalidValueException {
			String setLocation = null;
			Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
			if (admin != null) {
				JSONObject response = admin.set(LocationResource.LOCATION, DataResource.VALUE, location).getResponse();
				setLocation = String.valueOf(response.opt(DataResource.VALUE));
			}
			return setLocation;
		}

		@Override
		public ServiceProvider.LifecycleStatus getStatus() {
			ServiceProvider.LifecycleStatus status = null;
			Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
			if (admin != null) {
				JSONObject response = admin.get(ServiceProvider.LIFECYCLE_STATUS, DataResource.VALUE).getResponse();
				status = ServiceProvider.LifecycleStatus.valueOf(String.valueOf(response.opt(DataResource.VALUE)));
			}
			return status;
		}
		
		@Override
		public ServiceProvider.LifecycleStatus setStatus(ServiceProvider.LifecycleStatus status)
				throws InvalidValueException {
			ServiceProvider.LifecycleStatus setStatus = null;
			Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
			if (admin != null) {
				JSONObject response = admin.set(ServiceProvider.LIFECYCLE_STATUS, DataResource.VALUE, status).getResponse();
				setStatus = ServiceProvider.LifecycleStatus.valueOf(String.valueOf(response.opt(DataResource.VALUE)));
			}
			return setStatus;
		}
		
		@Override
		public boolean isAccessible() {
			return true;
		}

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

	@Override
	public void process(ServiceProviderProcessableData<?> data) {
		if (data == null) 
			return;
		
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
					LOG.error(e.getMessage(),e);
				}
			}
			if (service == null) {
				LOG.warn("Service '%s' not found", serviceId);
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
				LOG.error(e.getMessage(),e);
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

		String profile = super.modelInstance.getProfile();
		if(profile ==null) 
			profile =  ResourceConfig.ALL_PROFILES;
		
		List<ResourceConfig> resourceConfigs = super.getModelInstance().configuration(
				).getResourceConfigs(profile, ServiceProvider.ADMINISTRATION_SERVICE_NAME);

//		int index = -1;

		if (admin.getResource(LIFECYCLE_STATUS) == null) {
			ResourceBuilder statusResourceBuilder = super.getModelInstance().getResourceBuilder(
					super.getModelInstance().configuration().getResourceDescriptor(
							).withResourceType(PropertyResource.class
							).withResourceName(LIFECYCLE_STATUS
							).withDataType(ServiceProvider.LifecycleStatus.class
							).withDataValue(ServiceProvider.LifecycleStatus.INACTIVE
							).withHidden(true
							).withUpdatePolicy(AUTO
							).withModifiable(UPDATABLE
							).withProfile(profile), buildPolicy);
			statusResourceBuilder.configureName(LIFECYCLE_STATUS);
			admin.addResource(statusResourceBuilder);
		}
		if (admin.getResource(FRIENDLY_NAME) == null) {
			ResourceBuilder friendlyNameResourceBuilder = null;
			ResourceConfig rc = resourceConfigs.stream()
					.filter(r -> FRIENDLY_NAME.equals(r.getName()))
					.findFirst().orElse(null);
			if (rc != null) {
				friendlyNameResourceBuilder = super.getModelInstance().getResourceBuilder(rc);
			} else {
				friendlyNameResourceBuilder = super.getModelInstance().getResourceBuilder(
						super.getModelInstance().configuration().getResourceDescriptor(
							).withResourceType(PropertyResource.class
							).withResourceName(FRIENDLY_NAME
							).withDataType(String.class
							).withHidden(false
							).withUpdatePolicy(AUTO
							).withModifiable(MODIFIABLE
							).withProfile(profile), buildPolicy);
			}
			friendlyNameResourceBuilder.configureName(FRIENDLY_NAME);
			admin.addResource(friendlyNameResourceBuilder);
		}
		if (admin.getResource(LOCATION) == null) {
			String defaultLocation=ModelInstance.defaultLocation(super.modelInstance.mediator());			
			ResourceBuilder locationResourceBuilder = null;
			ResourceConfig rc = resourceConfigs.stream()
					.filter(r -> LOCATION.equals(r.getName()))
					.findFirst().orElse(null);
			if (rc != null) {
				locationResourceBuilder = super.getModelInstance().getResourceBuilder(rc);
			}  else {
				locationResourceBuilder = super.getModelInstance().getResourceBuilder(
					super.getModelInstance().configuration().getResourceDescriptor(
							).withResourceType(LocationResource.class
							).withResourceName(LOCATION
							).withDataType(String.class
							).withDataValue(defaultLocation
							).withHidden(false
							).withModifiable(MODIFIABLE
							).withProfile(profile), 
					buildPolicy);
			}
			locationResourceBuilder.configureName(LocationResource.LOCATION);
			admin.addResource(locationResourceBuilder);
		}
		if (admin.getResource(BRIDGE) == null) {
			ResourceBuilder bridgeResourceBuilder = null;
			try {
				bridgeResourceBuilder = super.getModelInstance().getResourceBuilder(
						super.getModelInstance().configuration().getResourceDescriptor(
						    ).withResourceType(PropertyResource.class
							).withResourceName(BRIDGE
							).withDataType(String.class
							).withDataValue(super.modelInstance.mediator().getContext().getBundle().getSymbolicName()
							).withHidden(false
							).withUpdatePolicy(AUTO
							).withModifiable(FIXED
							).withProfile(profile),buildPolicy);
				bridgeResourceBuilder.configureName(BRIDGE);
				admin.addResource(bridgeResourceBuilder);
			} catch (Exception e) {
				LOG.debug("Unable to create the 'bridge' resource");
				bridgeResourceBuilder = null;
			}
		}
		if (admin.getResource(ICON) == null) {
			ResourceBuilder iconResourceBuilder = null;
			ResourceConfig rc = resourceConfigs.stream()
					.filter(r -> ICON.equals(r.getName()))
					.findFirst().orElse(null);
			if (rc != null) {
				iconResourceBuilder = super.getModelInstance().getResourceBuilder(rc);
			} else {
				iconResourceBuilder = super.getModelInstance().getResourceBuilder(
						super.getModelInstance().configuration().getResourceDescriptor(
							).withResourceType(PropertyResource.class
							).withResourceName(ICON
							).withDataType(String.class
						    ).withHidden(false
						    ).withUpdatePolicy(AUTO
						    ).withModifiable(MODIFIABLE
						    ).withProfile(profile), buildPolicy);
			}
			iconResourceBuilder.configureName(ICON);
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
			LOG.warn("'%s' service already exists", serviceName);

			return service;
		}
		byte buildPolicy = getModelInstance().configuration().getServiceBuildPolicy();

		if (!SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
				SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED)
				&& !this.serviceNames.contains(serviceName)) {
			LOG.warn("Incompatible build policy : unable to create the '%s' service",
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
			List<ResourceConfig> resources = super.modelInstance.configuration().getResourceConfigs(
					super.modelInstance.getProfile(), serviceName);

			if (super.modelInstance.getProfile() != null
					&& !ResourceConfig.ALL_PROFILES.equals(super.modelInstance.getProfile())) {
				resources.addAll(super.modelInstance.configuration().getResourceConfigs(ResourceConfig.ALL_PROFILES,
						serviceName));
			}
			ResourceConfig defaultResourceConfig = super.modelInstance.configuration().getDefaultResourceConfig(serviceName);

			if (defaultResourceConfig != null) {
				defaultResourceConfig.configureName(serviceName, serviceName);
			}
			if (resources == null) {
				resources = Collections.emptyList();
			}
			LOG.info("New service discovered for '%s' service provider : %s", super.name,
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
			LOG.debug("Unable to create the service '%s'", builder.getConfiguredName());
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

	protected Class<? extends ElementsProxy<Service>> getProxyType() {
		return ServiceProvider.class;
	}

	@Override
	public void start() {
		if (!super.getModelInstance().isRegistered()) {
			LOG.error("The resource model is not registered");
			return;
		}
		this.setStatus(ServiceProvider.LifecycleStatus.JOINING);
		try {
			super.start();
			this.doStart();

		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			this.stop();
			return;
		}
		this.setStatus(ServiceProvider.LifecycleStatus.ACTIVE);
		LOG.debug("ServiceProvider '%s' started", this.getName());
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
			LOG.error(e.getMessage(),e);
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

	@Override
	protected SnaLifecycleMessage.Lifecycle getRegisteredEvent() {
		return SnaLifecycleMessage.Lifecycle.PROVIDER_APPEARING;
	}

	@Override
	protected SnaLifecycleMessage.Lifecycle getUnregisteredEvent() {
		return SnaLifecycleMessage.Lifecycle.PROVIDER_DISAPPEARING;
	}

	@Override
	public ServiceProviderProxy getProxy(List<MethodAccessibility> methodAccessibilities) {
		return new ServiceProviderProxy(super.modelInstance.mediator(), super.getName());
	}

	@Override
	protected Service getElementProxy(AccessTree<?> tree, ServiceImpl element) throws ModelElementProxyBuildException {
		Service service = element.getProxy(tree);
		return service;
	}

	@Override
	protected ModelElementProxyWrapper getWrapper(ServiceProviderProxy proxy, ImmutableAccessTree tree) {
		return new ServiceProviderProxyWrapper(proxy, tree);
	}

}

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.execution.DefaultErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.PrimitiveDescription;
import org.eclipse.sensinact.gateway.common.primitive.Typable;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.BufferMidCallback;
import org.eclipse.sensinact.gateway.core.message.MidCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.ScheduledBufferMidCallback;
import org.eclipse.sensinact.gateway.core.message.ScheduledMidCallback;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaNotificationMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessage;
import org.eclipse.sensinact.gateway.core.message.SubscriptionFilter;
import org.eclipse.sensinact.gateway.core.message.UnaryMidCallback;
import org.eclipse.sensinact.gateway.core.method.AbstractAccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;

/**
 * Basis {@link Resource} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceImpl extends
		ModelElement<ModelInstance<?>, ResourceProxy, ResourceProcessableContainer<?>, Attribute, AttributeDescription>
		implements Typable<Resource.Type> {
	public class ResourceProxyWrapper extends ModelElementProxyWrapper implements Typable<Resource.Type> {
		ResourceProxyWrapper(ResourceProxy proxy, ImmutableAccessTree tree) {
			super(proxy, tree);
		}

		public Resource.Type getType() {
			return ResourceImpl.this.getType();
		}

		@Override
		public boolean isAccessible() {
			return true;
		}

		public Description getDescription() {
			return new Description() {
				@Override
				public String getName() {
					return ResourceImpl.this.getName();
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
					buffer.append("type");
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COLON);
					buffer.append(JSONUtils.QUOTE);
					buffer.append(ResourceProxyWrapper.this.getType().name());
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.CLOSE_BRACE);
					return buffer.toString();
				}

				@Override
				public String getJSONDescription() {
					try {
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
						buffer.append("type");
						buffer.append(JSONUtils.QUOTE);
						buffer.append(JSONUtils.COLON);
						buffer.append(JSONUtils.QUOTE);
						buffer.append(ResourceProxyWrapper.this.getType().name());
						buffer.append(JSONUtils.QUOTE);
						buffer.append(JSONUtils.COMMA);
						buffer.append(JSONUtils.QUOTE);
						buffer.append("attributes");
						buffer.append(JSONUtils.QUOTE);
						buffer.append(JSONUtils.COLON);
						buffer.append(JSONUtils.OPEN_BRACKET);
						int index = 0;

						Enumeration<AttributeDescription> enumeration = ResourceProxyWrapper.this.elements();

						while (enumeration.hasMoreElements()) {
							buffer.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
							buffer.append(enumeration.nextElement().getJSONDescription());
							index++;
						}
						buffer.append(JSONUtils.CLOSE_BRACKET);

						buffer.append(JSONUtils.COMMA);
						buffer.append(JSONUtils.QUOTE);
						buffer.append("accessMethods");
						buffer.append(JSONUtils.QUOTE);
						buffer.append(JSONUtils.COLON);
						buffer.append(JSONUtils.OPEN_BRACKET);

						index = 0;
						int pos = 0;
						AccessMethod.Type[] types = AccessMethod.Type.values();

						for (; index < types.length; index++) {
							AccessMethod m = proxy.getAccessMethod(types[index].name());
							if (m != null && m.size() > 0) {
								buffer.append(pos > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
								buffer.append(m.getDescription().getJSONDescription());
								pos++;
							}
						}
						buffer.append(JSONUtils.CLOSE_BRACKET);
						buffer.append(JSONUtils.CLOSE_BRACE);
						return buffer.toString();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
		}
	}

	/**
	 * {@link AccessMethod}s of this {@link Resource}
	 */
	protected final Map<AccessMethod.Type, AccessMethod> methods;

	/**
	 * Path of registered {@link LinkedResourceImpl} referring to this resource
	 */
	protected final List<String> links;

	/**
	 * Extended {@link Resource} type of this implementation
	 */
	protected final Class<? extends Resource> resourceType;

	/**
	 * the name of the default attribute of this resource
	 */
	protected String defaultAttribute;

	/**
	 * this ResourceImpl's update policy : NONE : updates are made by the way of the
	 * client request INIT : the first upate is made by the way of the client
	 * request and is automatic after that AUTO : updates are automatic
	 */
	protected Resource.UpdatePolicy updatePolicy;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param resourceType
	 * @param service
	 */
	protected ResourceImpl(ModelInstance<?> modelInstance, ResourceConfig resourceConfig, ServiceImpl service) {
		super(modelInstance, service, UriUtils.getUri(
				new String[] { service.getPath(), resourceConfig.getName(service.getName()) }));
		this.methods = new HashMap<AccessMethod.Type, AccessMethod>();
		this.links = new ArrayList<String>();
		this.resourceType = resourceConfig.getTypeConfig().getResourceImplementedInterface();
		this.setUpdatePolicy(resourceConfig.getUpdatePolicy());
		buildAttributes(resourceConfig);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.ModelElementOld#
	 *      proces(org.eclipse.sensinact.gateway.core.ProcessableData)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void process(ResourceProcessableContainer data) {
		if (data == null) {
			return;
		}
		Iterator<ResourceProcessableData> iterator = data.iterator();

		while (iterator.hasNext()) {
			ResourceProcessableData resourceProcessableData = iterator.next();
			String attributeId = resourceProcessableData.getAttributeId();
			String metadataName = resourceProcessableData.getMetadataId();
			try {
				this.update(attributeId, metadataName, resourceProcessableData.getData(),
						resourceProcessableData.getTimestamp());
			} catch (InvalidValueException e) {
				super.modelInstance.mediator().error(e, "'%s' resource cannot be updated", getPath());
			}
		}
	}

	/**
	 * Updates this ResourceImpl instance. If the attributeName parameter is null
	 * the default one is targeted. If the metadataName argument is not null the
	 * {@link Metadata} with the specified name is updated
	 * 
	 * @param attributeName
	 *            the name of the attribute to update the value of
	 * @param metadataName
	 *            the name of the metadata to update the value of
	 * @param value
	 *            the value to update
	 * @param timestamp
	 *            the update timestamp
	 * 
	 * @throws InvalidValueException
	 */
	protected void update(String attributeName, String metadataName, Object value, long timestamp)
			throws InvalidValueException {
		if (value == null) {
			super.modelInstance.mediator()
					.warn(new StringBuilder().append("Null object value : unable to update resource '")
							.append(getPath()).append("'").toString());
			return;
		}
		String name = attributeName == null ? this.getDefault() : attributeName;

		if (name == null) {
			super.modelInstance.mediator()
					.warn(new StringBuilder().append("Null attribute name : unable to update resource '")
							.append(getPath()).append("'").toString());
			return;
		}
		Attribute attribute = this.getAttribute(name);
		byte buildPolicy = super.getModelInstance().configuration().getResourceBuildPolicy();

		if (attribute == null && SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
				SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED)) {
			attribute = new Attribute(super.modelInstance.mediator(), this, name, String.class, null,
					Modifiable.UPDATABLE, false);

			if (!this.addAttribute(attribute)) {
				super.modelInstance.mediator().warn("Error when creating attribute '%s': unable to update resource '%s",
						name, getPath());
				return;
			}
		}
		if (attribute == null) {
			super.modelInstance.mediator().warn(new StringBuilder().append("Null attribute '").append(name)
					.append("': unable to update resource '").append(getPath()).append("'").toString());

			return;
		}
		if (metadataName != null) {
			Metadata metadata = attribute.get(metadataName);

			if (metadata == null && SensiNactResourceModelConfiguration.BuildPolicy.isBuildPolicy(buildPolicy,
					SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED)) {
				metadata = new Metadata(super.modelInstance.mediator(), metadataName, String.class, null,
						Modifiable.UPDATABLE);
				attribute.addMetadata(metadata);
			}
			if (metadata == null) {
				super.modelInstance.mediator().warn(new StringBuilder().append("Null metadata ").append(metadataName)
						.append(": unable to update resource '").append(getPath()).append("'").toString());
				return;
			}
			attribute.setMetadataValue(metadataName, value);

		} else {
			long valueTimestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
			attribute.setValue(value, valueTimestamp);
		}
	}

	/**
	 * Creates the set of {@link Attribute}s of this {@link ResourceImpl} according
	 * to the configuration of the {@link ResourceConfig} passed as parameter
	 * 
	 * @param resourceConfig
	 *            the {@link ResourceConfig} holding the set of
	 *            {@link AttributeBuilder}s
	 */
	public void buildAttributes(ResourceConfig resourceConfig) {
		try {
		String parentPath = UriUtils.getParentUri(super.getPath());
		String parentName = UriUtils.getLeaf(parentPath);

		String defaultAttributeName = resourceConfig.getTypeConfig()
				.<String>getConstantValue(Resource.ATTRIBUTE_DEFAULT_PROPERTY, false);
		this.setDefault(defaultAttributeName);

		List<AttributeBuilder> builders = resourceConfig.getAttributeBuilders(parentName);

		int length = builders == null ? 0 : builders.size();
		int index = 0;

		for (; index < length; index++) {
			Attribute attribute = null;
			try {
				attribute = builders.get(index).getAttribute(super.modelInstance.mediator(), 
					this, resourceConfig.getTypeConfig());

			} catch (InvalidAttributeException e) {
				super.modelInstance.mediator().error(e);
			}
			if (attribute != null) {
				if (attribute.getName().equals(defaultAttributeName)) {
					try {
						Metadata metadata = new Metadata(super.modelInstance.mediator(), 
							Attribute.NICKNAME, String.class, this.getName(), Modifiable.FIXED);

						attribute.addMetadata(metadata);

					} catch (InvalidValueException e) {
						super.modelInstance.mediator().error(e);
					}
				}
				this.addAttribute(attribute);
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * Returns an {@link Executable} whose execution returns the value of this
	 * {@link ResourceImpl}'s attribute whose name is passed as parameter.
	 * 
	 * @param attributeName the name of the {@link Attribute} of this {@link ResourceImpl} for 
	 * which to create a value extractor
	 * 
	 * @return an {@link Executable} value extractor for the specified {@link Attribute}
	 */
	public Executable<Void, Object> getResourceValueExtractor(String attributeName) {
		String defaultAttributeName = attributeName==null?getDefault():attributeName;
		final Attribute attribute = getAttribute(defaultAttributeName);

		return new Executable<Void, Object>() {
			@Override
			public Object execute(Void parameter) throws Exception {
				if (attribute != null) {
					return attribute.getValue();
				}
				return null;
			}
		};
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.PrimitiveCollection#
	 *      set(java.lang.String, java.lang.Object)
	 */
	public AttributeDescription set(String name, Object value) throws InvalidValueException {
		AttributeDescription description = null;
		if (name == null) {
			return description;
		}
		Attribute attribute = this.getAttribute(name);
		if (attribute != null) {
			if (!Modifiable.MODIFIABLE.equals(attribute.getModifiable())) {
				throw new InvalidValueException(
						new StringBuilder().append(name).append(" attribute is not modifiable").toString());
			}
			if (attribute.getLocked()) {
				throw new InvalidValueException(new StringBuilder().append(name)
						.append(" attribute has been locked by an action trigger").toString());
			}
			attribute.setValue(value);
			description = (AttributeDescription) attribute.getDescription();
		}
		return description;
	}

	/**
	 * Sets the value of the default {@link Attribute} of this Resource if it is
	 * defined and return the extended {@link Description} describing it
	 * 
	 * @return the {@link AttributeDescription} describing this Resource's default
	 *         {@link Attribute}
	 */
	public AttributeDescription set(Object value) throws InvalidValueException {
		return this.set(this.getDefault(), value);
	}

	/**
	 * Adds the given {@link Attribute} to this Resource. The {@link Attribute} will
	 * not be added if there exist one with the same name. In order to update an
	 * existing {@link Attribute} the set methods must be used.
	 * 
	 * @param attribute
	 *            the {@link Attribute} to add
	 */
	public boolean addAttribute(Attribute attribute) {
		return super.addElement(attribute);
	}

	/**
	 * Removes the {@link Attribute} whose name is passed as parameter
	 * 
	 * @param primitive
	 *            the name of the {@link Attribute} to remove
	 */
	public boolean removeAttribute(String attributeName) {
		if (attributeName == null || Resource.NAME.intern() == attributeName.intern()
				|| Resource.TYPE.intern() == attributeName.intern()
				|| DataResource.VALUE.intern() == attributeName.intern()) {
			return false;
		}
		return (super.removeElement(attributeName) != null);
	}

	/**
	 * Returns the {@link Attribute} of this ResourceImpl, whose name is the same as
	 * the specified one
	 * 
	 * @param name
	 *            the name of the searched {@link Attribute}
	 * @return the {@link Attribute} with the specified name
	 */
	public Attribute getAttribute(String name) {
		return super.element(name);
	}

	/**
	 * Returns the {@link AttributeDescription} of the {@link Attribute} owned by
	 * this ResourceImpl and whose name is passed as parameter
	 * 
	 * @param name
	 *            the name of the {@link Attribute} to return the description of
	 * @return the {@link AttributeDescription} of the {@link Attribute} whose name
	 *         is passed as parameter or null if this ResourceImpl does not own a
	 *         {@link Attribute} with the specified name
	 */
	public AttributeDescription getDescription(String name) {
		AttributeDescription description = null;
		Attribute attribute = getAttribute(name);
		if (attribute != null) {
			description = attribute.getDescription();
		}
		return description;
	}

	/**
	 * Returns the array of {@link Description}s of all owned {@link Describable}s
	 * 
	 * @return the {@link Description}s of all owned {@link Describable}s
	 */
	public List<AttributeDescription> getAllDescriptions() {
		List<AttributeDescription> descriptions = new ArrayList<AttributeDescription>();

		synchronized (super.elements) {
			Iterator<Attribute> iterator = super.elements.iterator();
			while (iterator.hasNext()) {
				descriptions.add(iterator.next().<AttributeDescription>getDescription());
			}
		}
		return descriptions;
	}

	/**
	 * Returns the name of the default {@link Attribute} of this ResourceImpl
	 * 
	 * @return the name of this ResourceImpl's default {@link Attribute}
	 */
	public String getDefault() {
		return this.defaultAttribute;
	}

	/**
	 * Defines the name of the default {@link Attribute} of this ResourceImpl
	 * 
	 * @param defaultAttribute
	 *            the name of the default {@link Attribute}
	 */
	public void setDefault(String defaultAttribute) {
		this.defaultAttribute = defaultAttribute;
	}

	/**
	 * This ResourceImpl is hidden if its default attribute is
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if this ResourceImpl is hidden;</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean isHidden() {
		if (this.defaultAttribute == null) {
			return false;
		}
		try{
			Attribute attribute = this.getAttribute(defaultAttribute);
			return attribute.isHidden();
		}catch (Exception e){
			return false;
		}

	}

	/**
	 * Returns the extended {@link Resource} type of this resource
	 * 
	 * @return the extended {@link Resource} type of this resource
	 */
	@SuppressWarnings("unchecked")
	public <S extends Resource> Class<S> getResourceType() {
		return (Class<S>) this.resourceType;
	}

	@Override
	public String getName() {
		return (String) this.getAttribute(Resource.NAME).getValue();
	}

	@Override
	public Resource.Type getType() {
		return (Resource.Type) this.getAttribute(Resource.TYPE).getValue();
	}

	/**
	 * Registers a listener for the attribute whose name is passed as parameter
	 * 
	 * @param attributeName
	 *            the name of the attribute to listen
	 * @param recipient
	 *            the {@link Recipient} to callback
	 * @param conditions
	 *            the set of {@link Constraint}s applied on the listened events
	 * 
	 * @return a registration identifier if the listener has been registered
	 *         properly; null otherwise
	 */
	protected String listen(String attributeName, Recipient recipient, Set<Constraint> conditions,
			int policy, MidCallback.Type type, long lifetime, int buffer, int delay) {
		StringBuilder builder = new StringBuilder();
		builder = new StringBuilder();
		builder.append(this.getPath());
		builder.append(UriUtils.PATH_SEPARATOR);
		builder.append(attributeName);
		String filter = builder.toString();

		MidCallback.Type callbackType = type == null ? MidCallback.Type.UNARY : type;
		long timeout = lifetime <= 10000 ? MidCallback.ENDLESS : System.currentTimeMillis() + lifetime;
		int schedulerDelay = delay < 1000 ? 1000 : delay;
		int bufferSize = buffer < 10 ? 10 : buffer;

		MidCallback callback = null;

		switch (callbackType) {
		case BUFFERERIZED_AND_SCHEDULED:
			callback = new ScheduledBufferMidCallback(super.modelInstance.mediator(), 
				new DefaultErrorHandler(policy), recipient, timeout, schedulerDelay, bufferSize);
			break;
		case BUFFERIZED:
			callback = new BufferMidCallback(super.modelInstance.mediator(),
				new DefaultErrorHandler(policy), recipient, timeout, bufferSize);
			break;
		case SCHEDULED:
			callback = new ScheduledMidCallback(super.modelInstance.mediator(), 
				new DefaultErrorHandler(policy), recipient, timeout, schedulerDelay);
			break;
		case UNARY:
			callback = new UnaryMidCallback(super.modelInstance.mediator(), 
				new DefaultErrorHandler(policy), recipient, timeout);
			break;
		default:
			break;
		}
		if (callback != null) {
			super.modelInstance.registerCallback(new SubscriptionFilter(
				super.modelInstance.mediator(), filter, conditions), callback);
			return callback.getName();
		}
		return null;
	}

	/**
	 * Unregisters the listener whose callback's identifier is passed as parameter
	 * 
	 * @param callbackId
	 *            the callback identifier
	 */
	protected void unlisten(String callbackId) {
		super.modelInstance.unregisterCallback(callbackId);
	}

	/**
	 * Returns the {@link AccessMethod} of this resource whose
	 * {@link AccessMethod.Type} is passed as parameter
	 * 
	 * @param act
	 *            the {@link AccessMethod.Type} of the {@link AccessMethod} to
	 *            return
	 * @return the {@link AccessMethod} of this resource of the specified type
	 */
	public AccessMethod getAccessMethod(AccessMethod.Type method) {
		return this.methods.get(method);
	}

	/**
	 * Registers a {@link Signature} to create and to associate with the specified
	 * {@link AccessMethodExecutor}
	 * 
	 * @param type
	 *            the type of the {@link AccessMethod} associated to the signature
	 *            to create
	 * @param parameterNames
	 *            the array of parameter names of the signature to create
	 * @param parameterTypes
	 *            the array of parameter types of the signature to create
	 * @param executor
	 *            the {@link AccessMethodExecutor} to associate to the signature to
	 *            create
	 * @param policy
	 *            the execution policy of the {@link AccessMethodExecutor}
	 * @throws InvalidValueException
	 * @throws InvalidConstraintDefinitionException
	 */
	public AccessMethod registerExecutor(AccessMethod.Type type, Class<?>[] parameterTypes, String[] parameterNames,
			AccessMethodExecutor executor, AccessMethodExecutor.ExecutionPolicy policy) throws InvalidValueException {
		AccessMethod method = this.getAccessMethod(type);
		if (method != null) {
			((AbstractAccessMethod) method).addSignature(parameterTypes, parameterNames, executor, policy);
		}
		return method;
	}

	/**
	 * Registers the {@link Signature} passed as parameter and associates it with
	 * the specified {@link AccessMethodExecutor}
	 * 
	 * @param signature
	 *            the {@link Signature} to register
	 * @param executor
	 *            the {@link AccessMethodExecutor} to associate to the
	 *            {@link Signature} to register
	 * @param policy
	 *            the execution policy of the specified {@link AccessMethodExecutor}
	 * @throws InvalidConstraintDefinitionException
	 * @throws InvalidValueException
	 */
	public AccessMethod registerExecutor(Signature signature, AccessMethodExecutor executor,
			AccessMethodExecutor.ExecutionPolicy policy) {
		AccessMethod method = this.getAccessMethod(AccessMethod.Type.valueOf(signature.getName()));
		if (method != null) {
			((AbstractAccessMethod) method).addSignature(signature, executor, policy);
		}
		return method;
	}

	/**
	 * Returns the {@link Enumeration} of the set of {@link Attribute}s of this
	 * ResourceImpl
	 * 
	 * @return the the {@link Enumeration} of this ResourceImpl's {@link Attribute}s
	 */
	public Enumeration<Attribute> attributes() {
		return super.elements();
	}

	/**
	 * @param attribute
	 * @param value
	 * @param hasChanged
	 */
	@SuppressWarnings("rawtypes")
	protected void updated(Attribute attribute, Object value, boolean hasChanged) {
		if (!super.started.get() || attribute.isHidden()) {
			return;
		}
		String[] links = new String[this.links.size() + 1];

		synchronized (this.links) {
			this.links.toArray(links);
		}
		links[this.links.size()] = this.getPath();
		int index = 0;
		int length = links.length;

		JSONObject attributeDescription = new JSONObject(attribute.getDescription().getJSON());

		for (; index < length; index++) {
			StringBuilder buffer = new StringBuilder();
			buffer = new StringBuilder();
			buffer.append(links[index]);
			buffer.append(UriUtils.PATH_SEPARATOR);
			buffer.append(attribute.getName());

			String path = buffer.toString();

			SnaUpdateMessage message = SnaNotificationMessageImpl.Builder.<SnaUpdateMessage>notification(
					super.modelInstance.mediator(), SnaUpdateMessage.Update.ATTRIBUTE_VALUE_UPDATED, path);

			message.setNotification(attributeDescription);
			((AbstractSnaMessage) message).put("hasChanged", hasChanged, true);

			super.modelInstance.postMessage(message);
		}
	}

	/**
	 * Registers the path of a {@link LinkedResourceImpl} linked to this ResouceImpl
	 * instance
	 * 
	 * @param path
	 *            the path of the {@link LinkedResourceImpl} linked to this
	 *            ResouceImpl instance
	 */
	protected void registerLink(String path) {
		this.links.add(path);
	}

	@Override
	public void start() {
		if (!super.getModelInstance().isRegistered() || this.isHidden()) {
			// already registered or hidden resource
			return;
		}
		if (super.started.get()) {
			this.modelInstance.mediator().debug("%s already started", this.getName());
			return;
		}
		super.started.set(true);
		super.modelInstance.mediator().debug("'%s' resource registered", this.getName());

		String path = this.getPath();

		SnaLifecycleMessage notification = SnaNotificationMessageImpl.Builder.<SnaLifecycleMessage>notification(
				super.modelInstance.mediator(), SnaLifecycleMessage.Lifecycle.RESOURCE_APPEARING, path);

		JSONObject notificationObject = new JSONObject();

		notificationObject.put(SnaConstants.ADDED_OR_REMOVED, SnaLifecycleMessage.Lifecycle.RESOURCE_APPEARING.name());
		notificationObject.put(Resource.TYPE, this.getType());

		notification.setNotification(notificationObject);

		Attribute attribute = null;

		if (this.defaultAttribute != null && (attribute = this.getAttribute(this.defaultAttribute)) != null) {
			JSONObject jsonAttribute = new JSONObject(attribute.getDescription().getJSON());

			MetadataDescription[] metadataDescriptions = attribute.getAllDescriptions();

			int index = 0;
			int length = metadataDescriptions.length;

			for (; index < length; index++) {
				MetadataDescription metadataDescription = metadataDescriptions[index];
				String metadataName = metadataDescription.getName().intern();

				if (Modifiable.FIXED.equals(metadataDescription.getModifiable())
						&& Metadata.LOCKED.intern() != metadataName && Metadata.MODIFIABLE.intern() != metadataName
						&& Metadata.HIDDEN.intern() != metadataName && Attribute.NICKNAME.intern() != metadataName) {
					jsonAttribute.put(metadataDescription.getName(),
							PrimitiveDescription.toJson(metadataDescription.getType(), metadataDescription.getValue()));
				}
			}
			((SnaLifecycleMessageImpl) notification).put("initial", jsonAttribute);
		}
		super.modelInstance.postMessage(notification);
	}

	/**
	 * Deletes all associated access method and the link with its description object
	 */
	public void stop() {
		if (this.isHidden()) {
			return;
		}
		AccessMethod.Type[] types = AccessMethod.Type.values();

		int index = 0;
		int length = types == null ? 0 : types.length;
		for (; index < length; index++) {
			AccessMethod method = this.methods.remove(types[index].name());

			if (method != null) {
				((AbstractAccessMethod) method).stop();
			}
		}
		super.stop();
	}

	@Override
	protected <TASK> TASK passOn(String type, String uri, Object[] parameters) throws Exception {
		if (!type.equals(AccessMethod.GET) || this.getUpdatePolicy() == Resource.UpdatePolicy.NONE
				|| this.getUpdatePolicy() == Resource.UpdatePolicy.INIT) {
			if (type.equals(AccessMethod.GET) && this.getUpdatePolicy() == Resource.UpdatePolicy.INIT) {
				this.setUpdatePolicy(Resource.UpdatePolicy.AUTO);
			}
			return super.passOn(type, uri, parameters);
		}
		return null;
	}

	/**
	 * Registers the {@link AccessMethod} passed as parameter, mapped to the
	 * specified {@link AccessMethod.Type}
	 * 
	 * @param type
	 *            the {@link AccessMethod.Type} of the {@link AccessMethod} to
	 *            register
	 * @param method
	 *            the {@link AccessMethod} to register
	 */
	void registerMethod(AccessMethod.Type type, AccessMethod method) {
		if (this.methods.get(type) == null) {
			this.methods.put(type, method);
		}
	}

	@Override
	protected Class<? extends ElementsProxy<AttributeDescription>> getProxyType() {
		return this.resourceType;
	}

	@Override
	protected SnaLifecycleMessage.Lifecycle getRegisteredEvent() {
		return SnaLifecycleMessage.Lifecycle.RESOURCE_APPEARING;
	}

	@Override
	protected SnaLifecycleMessage.Lifecycle getUnregisteredEvent() {
		return SnaLifecycleMessage.Lifecycle.RESOURCE_DISAPPEARING;
	}

	/**
	 * Defines this resource's update policy
	 * 
	 * @param updatePolicy
	 *            this resource's update policy
	 */
	public void setUpdatePolicy(Resource.UpdatePolicy updatePolicy) {
		this.updatePolicy = updatePolicy;
	}

	/**
	 * Returns this resource's update policy
	 * 
	 * @return this resource's update policy
	 */
	public Resource.UpdatePolicy getUpdatePolicy() {
		return this.updatePolicy;
	}

	@Override
	public ResourceProxy getProxy(List<MethodAccessibility> methodAccessibilities) {
		ResourceProxy proxy = new ResourceProxy(super.modelInstance.mediator(), this, methodAccessibilities);
		return proxy;
	}

	@Override
	protected AttributeDescription getElementProxy(AccessTree<?> tree, Attribute element)
			throws ModelElementProxyBuildException {
		if (element.isHidden()) {
			return null;
		}
		return element.getDescription();
	}

	@Override
	protected ModelElementProxyWrapper getWrapper(ResourceProxy proxy, ImmutableAccessTree tree) {
		ResourceProxyWrapper wrapper = new ResourceProxyWrapper(proxy, tree);
		return wrapper;
	}
}

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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.InvalidTriggerException;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.TriggerArgumentBuilder;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;

/**
 * Service implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceImpl extends ModelElement<ModelInstance<?>, ServiceProxy, ServiceProcessableData<?>, ResourceImpl, Resource> {
	
	class ServiceProxyWrapper extends ModelElementProxyWrapper implements ResourceCollection {
		protected ServiceProxyWrapper(ServiceProxy proxy, ImmutableAccessTree tree) {
			super(proxy, tree);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.ResourceCollection#getResources()
		 */
		@Override
		public List<Resource> getResources() {
			return super.list();
		}

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.ResourceCollection#
		 *      getResource(java.lang.String)
		 */
		@Override
		public <R extends Resource> R getResource(String resource) {
			return (R) super.element(resource);
		}

		/**
		 * @inheritDoc
		 *
		 * @see java.lang.reflect.InvocationHandler# invoke(java.lang.Object,
		 *      java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
			Object result = null;
			if (this.proxy.getProxied().isAssignableFrom(method.getDeclaringClass())) {
				Object[] calledParameters = null;
				if (method.isVarArgs() && parameters != null && parameters.length == 1
						&& parameters[0].getClass().isArray()) {
					calledParameters = (Object[]) parameters[0];

				} else {
					calledParameters = parameters;
				}
				if (calledParameters.length > 0) {
					String resourceName = (String) calledParameters[0];
					calledParameters[0] = getResource(resourceName);
				}
				result = this.proxy.invoke(method.getName().toUpperCase(), calledParameters);
			} else {
				result = method.invoke(this, parameters);
			}
			if (result == this.proxy || result == this) {
				return proxy;
			}
			return result;
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
		@Override
		public Description getDescription() {
			return new Description() {
				@Override
				public String getName() {
					return ServiceImpl.this.getName();
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
					buffer.append("resources");
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COLON);
					buffer.append(JSONUtils.OPEN_BRACKET);
					int index = 0;

					Enumeration<Resource> enumeration = ServiceProxyWrapper.this.elements();

					while (enumeration.hasMoreElements()) {
						buffer.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
						buffer.append(enumeration.nextElement().getDescription().getJSON());
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
					buffer.append("resources");
					buffer.append(JSONUtils.QUOTE);
					buffer.append(JSONUtils.COLON);
					buffer.append(JSONUtils.OPEN_BRACKET);
					int index = 0;

					Enumeration<Resource> enumeration = ServiceProxyWrapper.this.elements();

					while (enumeration.hasMoreElements()) {
						buffer.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
						buffer.append(enumeration.nextElement().getDescription().getJSONDescription());
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
	 * the number of subscriptions made on {@link Resource}s which belong to this
	 * service
	 */
	protected int subscriptionsCount;

	/**
	 * Constructor
	 * 
	 * @param modelInstance the {@link ModelInstance} of the service to be instantiated
	 * @param name the name of the service to be instantiated
	 * @param serviceProvider the {@link ServiceProviderImpl} parent of the service to be
	 * instantiated
	 * 
	 * @throws InvalidServiceException
	 *             if an error occurred while instantiating the service
	 */
	protected ServiceImpl(ModelInstance<?> modelInstance, String name, ServiceProviderImpl serviceProvider)
			throws InvalidServiceException {		
		super(modelInstance, serviceProvider, UriUtils.getUri(new String[] { serviceProvider.getPath(), name }));
		this.subscriptionsCount = 0;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelElement#
	 *      process(org.eclipse.sensinact.gateway.common.primitive.ProcessableData)
	 */
	public void process(ServiceProcessableData<?> data) {
		if (data == null) {
			return;
		}
		String resourceId = data.getResourceId();
		if (resourceId == null) {
			super.modelInstance.mediator().warn("Resource identifier not found");
			return;
		}
		ResourceImpl resource = this.getResource(resourceId);
		if (resource == null) {			
			Class<?> dataType = null;
			for(Iterator<?> it = data.iterator();it.hasNext();) {
				ResourceProcessableData rpd = (ResourceProcessableData) it.next();
				if(rpd.getAttributeId()!=null && !DataResource.VALUE.equals(rpd.getAttributeId())) 
					continue;
				Object obj = rpd.getData();
				Class<?> clazz = obj==null?null:obj.getClass();
				if(clazz == null)
					break;
				if(clazz.isPrimitive()) {
		    		switch(clazz.getName()) {
			    		case "byte":
			    		case "short":
			    		case "int":
			    		case "long":
			    		case "float":
			    		case "double":
			    			dataType = double.class;
		    				break;
			    		case "boolean":
			    			dataType = boolean.class;
		    				break;
			    		case "char":
			    			dataType = String.class;
		    				break;
		    		}
		    	} else if (obj instanceof Number) {	    		
		        	switch(clazz.getName()) {
		    		case "java.lang.Byte":
		    		case "java.lang.Short":
		    		case "java.lang.Integer":
		    		case "java.lang.Long":
		    		case "java.lang.Float":
		    		case "java.lang.Double":
		    			dataType = double.class;
						break;
		    		default:
		    			dataType = String.class;
	    				break;
		        	}			
				} else 
					dataType = String.class;
				break;
			}
			ResourceDescriptor descriptor = super.getModelInstance().configuration(
				).getResourceDescriptor(
				).withServiceName(super.getName()
				).withResourceName(resourceId
				).withDataType(dataType);
			
			ResourceBuilder builder = super.getModelInstance().getResourceBuilder(descriptor,
					this.modelInstance.configuration().getResourceBuildPolicy());

			if (builder != null) {
				try {
					resource = this.addResource(builder);
				} catch (Exception e) {
					super.modelInstance.mediator().error(e);
				}
			}
		}
		if (resource == null) {
			super.modelInstance.mediator().warn("Resource '%s' not found for '%s' service", resourceId,
					super.getName());
			return;
		}
		resource.process(data);
	}

	/**
	 * Creates and returns a new {@link DataResource} associated to this ServiceImpl
	 * 
	 * @param resourceClass
	 *            extended {@link DataResource} type to instantiate
	 * @param name
	 *            the name of the {@link DataResource} to instantiate
	 * @param type
	 *            the type of the 'value' {@link Attribute} of the
	 *            {@link DataResource} to instantiate
	 * @param value
	 *            the value of the 'value' {@link Attribute} of the
	 *            {@link DataResource} to instantiate
	 * @param attributes
	 *            extended set of attributes to add to the resource to create
	 * @return a new created {@link DataResource} instance
	 * 
	 * @throws InvalidResourceException
	 *             if an error occurred while instantiating the new
	 *             {@link DataResource}
	 */
	public <D extends DataResource> ResourceImpl addDataResource(Class<D> resourceClass, String name, Class<?> type, Object value) 
		throws InvalidResourceException {
		ResourceDescriptor descriptor = super.getModelInstance().configuration().getResourceDescriptor()
				.withServiceName(this.getName()).withResourceName(name).withResourceType(resourceClass)
				.withDataType(type).withDataValue(value);

		ResourceBuilder builder = super.getModelInstance().getResourceBuilder(descriptor,
				this.modelInstance.configuration().getResourceBuildPolicy());

		return this.addResource(builder);
	}

	/**
	 * Creates and adds a new {@link ActionResource} to this service
	 * 
	 * @param name
	 *            the name of the {@link ActionResource} to instantiate
	 * @param resourceClass
	 *            extended {@link ActionResource} type to instantiate
	 * @param attributes
	 *            extended set of attributes to add to the resource to create
	 * @return a new created {@link ActionResource} instance
	 * 
	 * @throws InvalidResourceException
	 */
	public <D extends ActionResource> ResourceImpl addActionResource(String name, Class<D> resourceClass)
			throws InvalidResourceException {
		ResourceDescriptor descriptor = super.getModelInstance().configuration().getResourceDescriptor()
				.withServiceName(this.getName()).withResourceName(name).withResourceType(resourceClass);

		ResourceBuilder builder = super.getModelInstance().getResourceBuilder(descriptor,
				this.modelInstance.configuration().getResourceBuildPolicy());

		return this.addResource(builder);
	}

	/**
	 * Creates and adds a {@link LinkedResourceImpl} linked to the
	 * {@link ResourceImpl} passed as parameter to this service
	 * 
	 * @param resource
	 *            the {@link ResourceImpl} to which to link the
	 *            {@link LinkedResourceImpl} to instantiate
	 * @param name
	 *            the name of the {@link LinkedResourceImpl} to instantiate
	 * @return a new created {@link LinkedResourceImpl} instance
	 * 
	 * @throws InvalidResourceException
	 */
	public ResourceImpl addLinkedResource(String link, ResourceImpl targetedResource) throws InvalidResourceException {
		if (targetedResource == null || this.getResource(link) != null
				|| LinkedResourceImpl.class.isAssignableFrom(targetedResource.getClass())) {
			super.modelInstance.mediator().debug("Unable to create the linked resource : %s", link);
			return null;
		}
		if (targetedResource.getType() == Resource.Type.ACTION) {
			return addLinkedActionResource(link, targetedResource, true);
		}
		ResourceBuilder builder = super.getModelInstance()
				.createResourceBuilder(super.getModelInstance().configuration().getResourceDescriptor()
						.withResourceName(link).withResourceType(targetedResource.getResourceType()));

		ResourceImpl linkedResource = builder.buildLinkedResource(this, targetedResource);

		if (this.addResource(linkedResource)) {
			targetedResource.registerLink(linkedResource.getPath());
		}
		return linkedResource;
	}

	/**
	 * Creates and adds a {@link LinkedResourceImpl} linked to the
	 * {@link Resource.Type.ACTION} typed {@link ResourceImpl} passed as parameter
	 * to this service
	 * 
	 * @param resource
	 *            the {@link Resource.Type.ACTION} typed {@link ResourceImpl} to
	 *            which to link the {@link LinkedResourceImpl} to instantiate
	 * @param name
	 *            the name of the {@link LinkedResourceImpl} to instantiate
	 * @param copyActMethod
	 *            true if the {@link LinkedActMethod} of the created
	 *            {@link LinkedResourceImpl} is a simple copy of the
	 *            {@link ActMethod} of the targeted {@link ActionResource} ; if
	 *            false an empty {@link LinkedActMethod} (containing no signature)
	 *            is registered into the created {@link LinkedResourceImpl}
	 * @return a new created {@link LinkedResourceImpl} instance
	 * 
	 * @throws InvalidResourceException
	 */
	public ResourceImpl addLinkedActionResource(String link, ResourceImpl targetedResource, boolean copyActMethod)
			throws InvalidResourceException {
		if (targetedResource == null || targetedResource.getType() != Resource.Type.ACTION
				|| this.getResource(link) != null) {
			super.modelInstance.mediator().debug(new StringBuilder()
					.append("Unable to create the resource - Invalid Name :").append(link).toString());
			return null;
		}
		ResourceBuilder builder = super.getModelInstance()
				.createResourceBuilder(super.getModelInstance().configuration().getResourceDescriptor()
						.withResourceName(link).withResourceType(targetedResource.getResourceType()));

		ResourceImpl linkedResource = builder.buildLinkedActionResource(this, targetedResource, copyActMethod);

		if (this.addResource(linkedResource)) {
			targetedResource.registerLink(linkedResource.getPath());
			return linkedResource;
		}
		return null;
	}

	/**
	 * Creates and returns a new {@link ResourceImpl} and updates existing
	 * {@link ServiceProxy}s its {@link ResourceProxy}
	 * 
	 * @param builder
	 *            the {@link ResourceBuilder} containing the definition of the
	 *            resource to create
	 * @return a new created {@link Resource} instance
	 * 
	 * @throws InvalidResourceException
	 *             if an error occurred while instantiating the new {@link Resource}
	 */
	public ResourceImpl addResource(ResourceBuilder builder) throws InvalidResourceException {
		String resourceName = builder.getConfiguredName();
		ResourceImpl resource = null;

		if (resourceName == null || (resource = this.getResource(resourceName)) != null) {
			super.modelInstance.mediator().debug("The resource '%s' already exists", resourceName);
			return null;
		}
		if ((resource = builder.build(super.modelInstance, this)) != null && this.addResource(resource)) {
			return resource;
		}
		return null;
	}

	/**
	 * Add the {@link ResourceImpl} passed as parameter to this ServiceImpl.
	 * If this ServiceImpl is already registered, the added {@link ResourceImpl}
	 * is started
	 * 
	 * @param resource the {@link ResourceImpl} to be added and potentially 
	 * started
	 * 
	 * @return true if the {@link ResourceImpl} has been added - false otherwise
	 */
	private boolean addResource(ResourceImpl resource) {
		if (resource == null || !super.addElement(resource)) {
			return false;
		}
		if (super.modelInstance.isRegistered()) {
			resource.start();
		}
		return true;
	}

	/**
	 * Stop and remove from this ServiceImpl the {@link ResourceImpl} whose 
	 * name is passed as parameter
	 *  
	 * @param resource the name of the {@link ResourceImpl} to be removed
	 * 
	 * @return the removed {@link ResourceImpl}
	 */
	public ResourceImpl removeResource(String resource) {
		return super.removeElement(resource);
	}

	/**
	 * Registers an {@link AccessMethodTrigger} and links its execution to a
	 * resource to define the value of a state variable one
	 * 
	 * @param method the name of the AccessMethod for which a signature is provided
	 * @param resourceType the extended {@link Resource} type of the listened resource
	 * @param listened the name of the listened resource
	 * @param target the name of the state variable resource to link to the listened resource
	 * @param signature the signature to which attach the specified {@link AccessMethodTrigger}
	 * @param trigger the {@link AccessMethodTrigger} to call when the specified signature is called
	 * @param policy the {@link AccessMethodExecutor.ExecutionPolicy} defining the trigger 
	 * execution order            
	 * @throws InvalidValueException
	 */
	public void addTrigger(String listen, final String target, Signature signature, 
		final AccessMethodTrigger trigger, AccessMethodExecutor.ExecutionPolicy policy) 
				throws InvalidValueException {
		
		if (!(signature.getName().equals(AccessMethod.ACT) || signature.getName().equals(AccessMethod.SET))){
			throw new InvalidTriggerException(String.format("ACT or SET method expected"));
		}
		ResourceImpl listened = this.getResource(listen);
		final ResourceImpl variable = this.getResource(target);
		
		if(listened == null||variable == null) {
			throw new InvalidTriggerException("Both observed and updated Resources are required");
		}
		Class<? extends Resource> resourceType = listened.getResourceType();
		
		// ensures that the listened resource exists and is an action one
		// and that the targeted one exists and is a variable
		if ((signature.getName().equals(AccessMethod.ACT) && !ActionResource.class.isAssignableFrom(resourceType))
			||(signature.getName().equals(AccessMethod.SET) && !DataResource.class.isAssignableFrom(resourceType))
			|| !StateVariableResource.class.isAssignableFrom(variable.getResourceType())) {
			throw new InvalidTriggerException(String.format("An %s and a StateVariableResource were expected", resourceType.getSimpleName()));
		}
		final Attribute attribute = variable.getAttribute(variable.getDefault());
		if (Modifiable.FIXED.equals(attribute.getModifiable())) {
			throw new InvalidValueException("Trigger cannot modify the resource value");
		}
		attribute.lock();

		// registers the executor which calls the trigger
		listened.registerExecutor(signature, new AccessMethodExecutor() {
			/**
			 * @inheritDoc
			 * 
			 * @see Executable#execute(java.lang.Object)
			 */
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Void execute(AccessMethodResponseBuilder builder) throws Exception {
				Object result = null;
				switch (trigger.getArgumentBuilder()) {
				case TriggerArgumentBuilder.EMPTY:
					result = trigger.execute(new TriggerArgumentBuilder.Empty().build(null));
					break;
				case TriggerArgumentBuilder.PARAMETER:
					result = trigger.execute(new TriggerArgumentBuilder.Parameter(
						trigger.<Integer>getArgument()).build(builder));
					break;
				case TriggerArgumentBuilder.RESPONSE:
					result = trigger.execute(new TriggerArgumentBuilder.Response().build(builder));
					break;
				case TriggerArgumentBuilder.INTERMEDIATE:
					result = trigger.execute(new TriggerArgumentBuilder.Intermediate().build(builder));
					break;
				case TriggerArgumentBuilder.MODEL:
					result = trigger.execute(new TriggerArgumentBuilder.Model(
						trigger.<String>getArgument()).build(ServiceImpl.this).getValue());
					break;
				default:
					break;
				}
				if (trigger.passOn()) 
					ServiceImpl.this.passOn(AccessMethod.SET, variable.getPath(), new Object[] { DataResource.VALUE, result });
				
				attribute.setValue(result);
				builder.push(new JSONObject(attribute.getDescription().getJSON()));
				return null;
			}
		}, policy);
	}

	/**
	 * Returns an {@link Executable} whose execution returns the value of the
	 * {@link ResourceImpl} of this ServiceImpl, and whose name is passed as
	 * parameter. The value of the targeted {@link ResourceImpl} is the one of its
	 * default {@link Attribute} if it has been defined
	 * 
	 * @param resourceName
	 *            the name of the {@link ResourceImpl} for which to create a value
	 *            extractor
	 * @return an {@link Executable} value extractor for the specified
	 *         {@link ResourceImpl}
	 */
	public Executable<Void, Object> getResourceValueExtractor(String resourceName) {
		final Attribute attribute;
		String defaultAttributeName = null;
		ResourceImpl resource = this.getResource(resourceName);

		if (resource != null && (defaultAttributeName = resource.getDefault()) != null) {
			attribute = resource.getAttribute(defaultAttributeName);

		} else {
			attribute = null;
		}
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
	 * Passes on the invocation of a method of a resource of this service to its
	 * parent service provider
	 * 
	 * @param type
	 *            the type of the invoked method
	 * @param resource
	 *            the name of the targeted resource
	 * @param parameters
	 *            the objects array parameterizing the call
	 * @return the JSON formated result object
	 * @throws Exception
	 */
	@Override
	protected <TASK> TASK passOn(String type, String path, Object[] parameters) throws Exception {
		this.subscriptionsCount += (type == AccessMethod.UNSUBSCRIBE) ? -1 : 0;
		TASK task = super.passOn(type, path, parameters);
		this.subscriptionsCount += (type == AccessMethod.SUBSCRIBE) ? 1 : 0;
		return task;
	}

	/**
	 * Returns the {@link ResourceImpl} provided by this ServiceImpl, whose name is
	 * passed as parameter
	 * 
	 * @param resource
	 *            the name of the {@link ResourceImpl}
	 * @return the {@link ResourceImpl} with the specified name
	 */
	public ResourceImpl getResource(String resource) {
		return super.element(resource);
	}

	/**
	 * Returns the set of {@link ResourceImpl}s held by this ServiceImpl
	 * 
	 * @return the set of this ServiceImpl's {@link ResourceImpl}s
	 */
	public List<ResourceImpl> getResources() {
		synchronized (super.elements) {
			return Collections.<ResourceImpl>unmodifiableList(super.elements);
		}
	}

	/**
	 * Returns the number of subscriptions registered on this service's resources
	 * 
	 * @return the number of registered subscriptions for this service
	 */
	public int getSubscriptionsCount() {
		return this.subscriptionsCount;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getRegisteredEvent()
	 */
	@Override
	protected Lifecycle getRegisteredEvent() {
		return Lifecycle.SERVICE_APPEARING;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getUnregisteredEvent()
	 */
	@Override
	protected Lifecycle getUnregisteredEvent() {
		return Lifecycle.SERVICE_DISAPPEARING;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.ModelElement#getProxyType()
	 */
	@Override
	protected Class<? extends ElementsProxy<Resource>> getProxyType() {
		return Service.class;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.SensiNactResourceModelElement#
	 *      getProxy(java.util.List)
	 */
	@Override
	public ServiceProxy getProxy(List<MethodAccessibility> methodAccessibilities) {
		try {
			ServiceProxy proxy = new ServiceProxy(super.modelInstance.mediator(), super.getPath());
			proxy.buildMethods(null, methodAccessibilities);
			return proxy;

		} catch (Exception e) {
			super.modelInstance.mediator().error(e);
		}
		return null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.ModelElement#
	 *      getElementProxy(org.eclipse.sensinact.gateway.core.security.AccessTree,
	 *      org.eclipse.sensinact.gateway.common.primitive.Nameable)
	 */
	@Override
	protected Resource getElementProxy(AccessTree<?> tree, ResourceImpl element)
			throws ModelElementProxyBuildException {
		if (element.isHidden()) {
			return null;
		}
		return element.getProxy(tree);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.ModelElement#
	 *      getWrapper(org.eclipse.sensinact.gateway.core.ModelElementProxy,
	 *      org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree)
	 */
	@Override
	protected ModelElementProxyWrapper getWrapper(ServiceProxy proxy, ImmutableAccessTree tree) {
		ServiceProxyWrapper wrapper = new ServiceProxyWrapper(proxy, tree);
		return wrapper;
	}
}

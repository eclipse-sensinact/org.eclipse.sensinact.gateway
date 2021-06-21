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

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.message.MidCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaNotificationMessageImpl;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.ActMethod;
import org.eclipse.sensinact.gateway.core.method.GetMethod;
import org.eclipse.sensinact.gateway.core.method.LinkedActMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.SetMethod;
import org.eclipse.sensinact.gateway.core.method.Shortcut;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.SubscribeMethod;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeMethod;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * Builder of a {@link ResourceImpl}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceBuilder {
	private static final AccessMethod.Type GET = AccessMethod.Type.valueOf(AccessMethod.GET);
	private static final AccessMethod.Type SET = AccessMethod.Type.valueOf(AccessMethod.SET);
	private static final AccessMethod.Type ACT = AccessMethod.Type.valueOf(AccessMethod.ACT);
	private static final AccessMethod.Type SUBSCRIBE = AccessMethod.Type.valueOf(AccessMethod.SUBSCRIBE);
	private static final AccessMethod.Type UNSUBSCRIBE = AccessMethod.Type.valueOf(AccessMethod.UNSUBSCRIBE);
	private static final AccessMethod.Type DESCRIBE = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);

	protected final Mediator mediator;
	protected final ResourceConfig resourceConfig;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the associated {@link Mediator}
	 * @param resourceType
	 *            the extended {@link Resource} type of the resource to create
	 * @throws InvalidResourceException
	 */
	public ResourceBuilder(Mediator mediator, ResourceConfig resourceConfig) {
		this.mediator = mediator;
		this.resourceConfig = resourceConfig;
	}

	/**
	 * Configures the name of the resource to build
	 * 
	 * @param name
	 *            the name of the resource to build
	 */
	public void configureName(String name) {
		RequirementBuilder builder = new RequirementBuilder(AttributeBuilder.Requirement.VALUE, Resource.NAME);
		builder.put(ResourceConfig.ALL_TARGETS, name);
		this.resourceConfig.addRequirementBuilder(builder);
	}

	/**
	 * Returns the name of the resource to build
	 * 
	 * @param name the name of the resource to build
	 */
	public String getConfiguredName() {
		return this.resourceConfig.getName();
	}

	/**
	 * Configures the type of the 'value' attribute of the resource to build
	 * 
	 * @param name the name of the resource to build
	 */
	public void configureType(Class<?> type) {
		this.configureRequirement(DataResource.VALUE, AttributeBuilder.Requirement.TYPE, type);
	}

	/**
	 * Configures the value of the 'value' attribute of the resource to build
	 * 
	 * @param name the name of the resource to build
	 */
	public void configureValue(Object value) {
		if (value == null) 
			return;		
		this.configureRequirement(DataResource.VALUE, AttributeBuilder.Requirement.VALUE, value);
	}

	/**
	 * Builds a new {@link RequirementBuilder} to apply on the set of {@link AttributeBuilder}s 
	 * used to create the set of {@link Attribute}s of the resource to create
	 * 
	 * @param attributeName the name of the attribute on which the requirement will apply
	 * @param requirement the targeted {@link AttributeBuilder.Requirement}
	 * @param value the object value to set
	 */
	public void configureRequirement(String attributeName, AttributeBuilder.Requirement requirement, Object value) {
		RequirementBuilder builder = new RequirementBuilder(requirement, attributeName);
		builder.put(value);
		this.resourceConfig.addRequirementBuilder(builder);
	}

	/**
	 * Return this ResourceBuilder's {@link ResourceConfig}
	 * 
	 * @return this ResourceBuilder's {@link ResourceConfig}
	 */
	public ResourceConfig getResourceConfig() {
		return this.resourceConfig;
	}

	/**
	 * Builds and returns a new {@link ResourceImpl} connected to the
	 * {@link ServiceImpl} passed as parameter
	 * 
	 * @param service the service to which the {@link ResourceImpl} to create is
	 * connected to
	 * 
	 * @return a new {@link ResourceImpl}
	 * 
	 * @throws InvalidResourceException
	 */
	public ResourceImpl build(ModelInstance<?> modelInstance, ServiceImpl service) throws InvalidResourceException {
		String name = this.getConfiguredName();
		if (name == null) 
			throw new InvalidResourceException("Resource's name is needed");
		
		TypeConfig typeConfig = this.getResourceConfig().getTypeConfig();

		ResourceImpl resourceImpl = ReflectUtils.getInstance(typeConfig.getResourceBaseClass(),
			typeConfig.getResourceImplementedClass(), new Object[] { modelInstance, 
					this.getResourceConfig(), service });

		if (resourceImpl == null) 
			throw new InvalidResourceException(String.format("Unable to create the resource : %s", name));
		
		if (!resourceImpl.isHidden()) {
			try {
				this.buildMethods(resourceImpl);

			} catch (InvalidValueException e) {
				if (this.mediator.isErrorLoggable()) 
					this.mediator.error(e);				
				throw new InvalidResourceException("Error while creating methods", e);
			}
		}
		return resourceImpl;
	}

	/**
	 * Builds and returns a new {@link LinkedResourceImpl} connected to the
	 * {@link ServiceImpl} passed as parameter and linked to the
	 * {@link ResourceImpl} also passed as parameter
	 * 
	 * @param service the service to which the {@link LinkedResourceImpl} to create is
	 * connected to
	 * @param target the {@link ResourceImpl} to which the {@link LinkedResourceImpl}
	 * to create is linked to
	 * @param link the name of the {@link LinkedResourceImpl} to create
	 * 
	 * @return a new {@link LinkedResourceImpl}
	 * 
	 * @throws InvalidResourceException
	 */
	public ResourceImpl buildLinkedResource(ServiceImpl service, ResourceImpl target) throws InvalidResourceException {
		if (target == null) 
			throw new InvalidResourceException("Target resource is needed");
		
		LinkedResourceImpl linkedResourceImpl = new LinkedResourceImpl((ModelInstance<?>) target.getModelInstance(),
				this.getResourceConfig(), target, service);

		try {
			this.buildMethods(linkedResourceImpl);

		} catch (InvalidValueException e) {
			throw new InvalidResourceException(e.getMessage(), e);
		}
		return linkedResourceImpl;
	}

	/**
	 * Builds and returns a new {@link LinkedResourceImpl} connected to the
	 * {@link ServiceImpl} passed as parameter and linked to the
	 * {@link ResourceImpl} also passed as parameter
	 * 
	 * @param service the service to which the {@link LinkedResourceImpl} to create is
	 * connected to
	 * @param target the {@link ResourceImpl} to which the {@link LinkedResourceImpl}
	 * to create is linked to
	 * @param link the name of the {@link LinkedResourceImpl} to create
	 * @param copyActMethod true if the {@link LinkedActMethod} of the created
	 * {@link LinkedResourceImpl} is a simple copy of the {@link ActMethod} of 
	 * the targeted {@link ActionResource} ; if false an empty {@link LinkedActMethod} 
	 * (containing no signature) is registered into the created {@link LinkedResourceImpl}
	 * 
	 * @return a new {@link LinkedResourceImpl}
	 * 
	 * @throws InvalidResourceException
	 */
	public ResourceImpl buildLinkedActionResource(ServiceImpl service, ResourceImpl target, boolean copyActMethod)
			throws InvalidResourceException {
		LinkedResourceImpl linkedResourceImpl = (LinkedResourceImpl) this.buildLinkedResource(service, target);

		LinkedActMethod actMethod = null;

		actMethod = new LinkedActMethod(this.mediator, linkedResourceImpl.getPath(),
				(ActMethod) target.getAccessMethod(ACT), copyActMethod);

		linkedResourceImpl.registerMethod(ACT, actMethod);
		return linkedResourceImpl;
	}

	protected final void buildMethods(final ResourceImpl resource)
			throws InvalidResourceException, InvalidValueException {
		if (resource == null) {
			return;
		}
		// Get method
		GetMethod getMethod = null;
		// Get access method signature
		Signature getSignature = null;
		// Set method
		SetMethod setMethod = null;
		// Set access method signature
		Signature setSignature = null;
		// Subscribe method
		SubscribeMethod subscribeMethod = null;
		// Subscribe access method signature
		Signature subscribeSignature = null;
		// Unsubscribe method
		UnsubscribeMethod unsubscribeMethod = null;
		// Unsubscribe access method signature
		Signature unsubscribeSignature = null;

		getMethod = new GetMethod(this.mediator, resource.getPath(),
				this.getPreProcessingExecutor(resource, AccessMethod.GET));

		getSignature = new Signature(this.mediator, GET, new Class<?>[] { String.class },
				new String[] { "attributeName" });

		setMethod = new SetMethod(this.mediator, resource.getPath(),
				this.getPreProcessingExecutor(resource, AccessMethod.SET));
		
		setSignature = new Signature(this.mediator, SET, new Class<?>[] { String.class, Object.class },
				new String[] { "attributeName", "value" });

		subscribeMethod = new SubscribeMethod(this.mediator, resource.getPath(),
				this.getPreProcessingExecutor(resource, AccessMethod.SUBSCRIBE));

		subscribeSignature = new Signature(this.mediator, SUBSCRIBE,
			new Class<?>[] { String.class, Recipient.class, Set.class, String.class },
			new String[] { "attributeName", "listener", "conditions", "policy" });

		unsubscribeMethod = new UnsubscribeMethod(this.mediator, resource.getPath(),
				this.getPreProcessingExecutor(resource, AccessMethod.UNSUBSCRIBE));
		
		unsubscribeSignature = new Signature(this.mediator, UNSUBSCRIBE, new Class<?>[] { String.class, String.class },
				new String[] { "attributeName", "subscriptionId" });
		
		// Get method
		getMethod.addSignature(getSignature, getExecutor(resource), AccessMethodExecutor.ExecutionPolicy.BEFORE);

		// check that at least one attribute is modifiable before to
		// register the set method signature
		Enumeration<Attribute> attrs = resource.attributes();

		while (attrs.hasMoreElements()) {
			if (Modifiable.MODIFIABLE.equals(attrs.nextElement().getModifiable())) {
				// Set method
				setMethod.addSignature(setSignature, setExecutor(resource),
						AccessMethodExecutor.ExecutionPolicy.BEFORE );
				break;
			}
		}
		// Subscribe method
		subscribeMethod.addSignature(subscribeSignature, subscribeExecutor(resource),
				AccessMethodExecutor.ExecutionPolicy.BEFORE);

		// Unsubscribe method
		unsubscribeMethod.addSignature(unsubscribeSignature, unsubscribeExecutor(resource),
				AccessMethodExecutor.ExecutionPolicy.BEFORE);

		// Subscribe method - shortcuts
		Parameter conditionsParameter = new Parameter(this.mediator, "conditions", Set.class);
		conditionsParameter.setValue(Collections.emptySet());

		Parameter policyParameter = new Parameter(this.mediator, "policy", String.class);
		policyParameter.setValue(String.valueOf(ErrorHandler.Policy.DEFAULT_POLICY));

		Map<Integer, Parameter> fixedPolicyParameters = new HashMap<Integer, Parameter>();

		fixedPolicyParameters = new HashMap<Integer, Parameter>();
		fixedPolicyParameters.put(3, policyParameter);
		
		Shortcut subscribePolicyShortcut = new Shortcut(this.mediator, SUBSCRIBE,
			new Class<?>[] { String.class, Recipient.class, Set.class }, 
			new String[] { "attributeName", "listener", "conditions" },
				fixedPolicyParameters);

		subscribeMethod.addShortcut(subscribePolicyShortcut, subscribeSignature);

		Map<Integer, Parameter> fixedConditionsParameters = new HashMap<Integer, Parameter>();		
		fixedConditionsParameters.put(2, conditionsParameter);

		Shortcut subscribeConditionsAndPolicyShortcut = new Shortcut(this.mediator, SUBSCRIBE,
				new Class<?>[] { String.class, Recipient.class }, 
				new String[] { "attributeName", "listener" },
				fixedConditionsParameters);
		
		subscribeMethod.addShortcut(subscribeConditionsAndPolicyShortcut, subscribePolicyShortcut);
		String resourceDefaultAttribute = resource.getDefault();
		
		if (resourceDefaultAttribute != null) {
			Parameter nameParameter = new Parameter(this.mediator, "attributeName", String.class);
			nameParameter.setValue(resourceDefaultAttribute);

			Map<Integer, Parameter> fixedNameParameter = new HashMap<Integer, Parameter>();

			fixedNameParameter.put(0, nameParameter);

			// Get method - name parameter shortcut
			//Shortcut getAttributeShortcut = new Shortcut(this.mediator, GET, new Class<?>[0], new String[0], fixedNameParameter, true);

			// Get method - name parameter shortcut
			Shortcut getAttributeShortcut = new Shortcut(this.mediator, GET, new Class<?>[0], new String[0], fixedNameParameter);
			
			getMethod.addShortcut(getAttributeShortcut, getSignature);

			// Set method - name parameter shortcut
			// if the default attribute is modifiable
			Attribute defaultAttribute = resource.getAttribute(resourceDefaultAttribute);

			if (defaultAttribute != null && Modifiable.MODIFIABLE.equals(defaultAttribute.getModifiable())) {
				
				Shortcut setAttributeShortcut = new Shortcut(this.mediator, SET, new Class<?>[] { Object.class },
						new String[] { DataResource.VALUE }, fixedNameParameter);

				setMethod.addShortcut(setAttributeShortcut, setSignature);
			}

			Shortcut subscribeNameConditionsAndPolicyShortcut = new Shortcut(this.mediator, SUBSCRIBE,
					new Class<?>[] { Recipient.class }, 
					new String[] { "listener" }, fixedNameParameter);

			Shortcut subscribeNameAndPolicyShortcut = new Shortcut(this.mediator, SUBSCRIBE,
					new Class<?>[] { Recipient.class, Set.class }, 
					new String[] { "listener", "conditions" },
					fixedNameParameter);

			Shortcut subscribeNameShortcut = new Shortcut(this.mediator, SUBSCRIBE,
					new Class<?>[] { Recipient.class, Set.class, String.class }, 
					new String[] { "listener", "conditions", "policy" },
					fixedNameParameter);
			
			subscribeMethod.addShortcut(subscribeNameConditionsAndPolicyShortcut, subscribeConditionsAndPolicyShortcut);
			subscribeMethod.addShortcut(subscribeNameAndPolicyShortcut, subscribePolicyShortcut);
			subscribeMethod.addShortcut(subscribeNameShortcut, subscribeSignature);

			Shortcut unsubscribeNameShortcut = new Shortcut(this.mediator, UNSUBSCRIBE, new Class<?>[] { String.class },
					new String[] { "subscriptionId" }, fixedNameParameter);

			unsubscribeMethod.addShortcut(unsubscribeNameShortcut, unsubscribeSignature);
		}
		if (ActionResource.class.isAssignableFrom(resource.getResourceType())
				&& !LinkedResourceImpl.class.isAssignableFrom(resource.getClass())) {
			ActMethod actMethod = new ActMethod(this.mediator, resource.getPath(),
					this.getPreProcessingExecutor(resource, AccessMethod.ACT),
					this.getActPostProcessingExecutor(resource));

			resource.registerMethod(ACT, actMethod);
		}
		resource.registerMethod(GET, getMethod);
		resource.registerMethod(SET, setMethod);
		resource.registerMethod(SUBSCRIBE, subscribeMethod);
		resource.registerMethod(UNSUBSCRIBE, unsubscribeMethod);
	}

	private AccessMethodExecutor getPreProcessingExecutor(final ResourceImpl resource, final String type) {
		return new AccessMethodExecutor() {
			@Override
			public Void execute(AccessMethodResponseBuilder parameter) throws Exception {
				Object resultObject = resource.passOn(type, resource.getPath(), parameter.getParameters());

				if (resultObject != null && resultObject != AccessMethod.EMPTY
						&& JSONObject.class.isAssignableFrom(resultObject.getClass())) {
					JSONObject result = (JSONObject) resultObject;
					result.remove("taskId");
					if (JSONObject.NULL.equals(result.opt("result"))) {
						result.remove("result");
					}
					parameter.setAccessMethodObjectResult(result);
				}
				return null;
			}
		};
	}

	private AccessMethodExecutor getActPostProcessingExecutor(final ResourceImpl resource) {
		return new AccessMethodExecutor() {
			@Override
			public Void execute(AccessMethodResponseBuilder parameter) throws Exception {
				SnaUpdateMessage message = SnaNotificationMessageImpl.Builder.<SnaUpdateMessage>notification(
					mediator, SnaUpdateMessage.Update.ACTUATED, resource.getPath());

				JSONObject notification = new JSONObject();
				notification.put(Metadata.TIMESTAMP, System.currentTimeMillis());
				notification.put(Resource.TYPE, "object");
				notification.put(DataResource.VALUE, parameter.getAccessMethodObjectResult());
				message.setNotification(notification);

				resource.getModelInstance().postMessage(message);
				return null;
			}
		};
	}

	final private AccessMethodExecutor getExecutor(final ResourceImpl resource) {
		return new AccessMethodExecutor() {
			@Override
			public Void execute(AccessMethodResponseBuilder snaResult) throws Exception {
				JSONObject result = null;
				AttributeDescription description = resource.getDescription((String) snaResult.getParameter(0));

				if (description != null) {
					result = new JSONObject(description.getJSON());
					snaResult.setAccessMethodObjectResult(result);
				}
				return null;
			}
		};
	}

	final private AccessMethodExecutor setExecutor(final ResourceImpl resource) {
		return new AccessMethodExecutor() {
			@Override
			public Void execute(AccessMethodResponseBuilder snaResult) throws Exception {
				Object[] parameters = snaResult.getParameters();
				int length = parameters == null?0:parameters.length;	
				AttributeDescription desc = resource.set((String) parameters[0], (Object)parameters[1]);
				JSONObject result = new JSONObject(desc.getJSON());
				snaResult.setAccessMethodObjectResult(result);
				return null;
			}
		};
	}

	final private AccessMethodExecutor subscribeExecutor(final ResourceImpl resource) {
		return new AccessMethodExecutor() {
			@Override
			public Void execute(AccessMethodResponseBuilder snaResult) throws Exception {
				String attributeName = (String) snaResult.getParameter(0);
				Attribute attribute = (Attribute) resource.getAttribute(attributeName);
				if (attribute == null) {
					throw new InvalidAttributeException(new StringBuilder().append(
						"unknown attribute :").append(attributeName).toString());
				}
				Object[] parameters = snaResult.getParameters();
				if (parameters == null || parameters.length < 2) {
					throw new IllegalArgumentException("Minimum set of parameters expected: attribute name and recipient");
				}
				Recipient recipient = null;
				Set<Constraint> conditions = null;
				MidCallback.Type type = null;
				int policy = ErrorHandler.Policy.DEFAULT_POLICY;
				long lifetime = 0;
				int buffer = 0;
				int delay = 0;

				switch (parameters.length) {
					case 8:
						buffer = ((Integer) parameters[6]).intValue();
						delay = ((Integer) parameters[7]).intValue();
					case 6:
						lifetime = ((Long) parameters[5]).longValue();
					case 5:
						type = (MidCallback.Type) parameters[4];
					case 4:
						if(parameters[3].getClass() == String.class) {
							try {
								policy = Integer.parseInt((String) parameters[3]);
							} catch(NumberFormatException e) {							
								policy = 0x000000;
								String[] policies = ((String) parameters[3]).split("|");
								for(int index = 0;index < policies.length;index++) {
									switch((policies[index]).trim()) {
									case "CONTINUE" : policy |= ErrorHandler.Policy.CONTINUE;
									break;
									case "STOP" : policy |= ErrorHandler.Policy.STOP;
									break;
									case "ROLLBACK" : policy |= ErrorHandler.Policy.ROLLBACK;
									break;
									case "IGNORE" : policy |= ErrorHandler.Policy.IGNORE;
									break;
									case "ALTERNATIVE" : policy |= ErrorHandler.Policy.ALTERNATIVE;
									break;
									case "LOG" : policy |= ErrorHandler.Policy.LOG;
									break;
									}								
								}
							}
						} else 
						policy = ((Integer) parameters[3]).intValue();		
					case 3:
						conditions = (Set) parameters[2];
					case 2:
						recipient = (Recipient) parameters[1];
						break;
					default:
						throw new IllegalArgumentException("Invalid parameters");
				}
				String callbackId = resource.listen(attributeName, recipient, conditions, policy, type, 
						lifetime, buffer, delay);

				if (callbackId != null) {
					JSONObject result = new JSONObject();
					result.put("subscriptionId", callbackId);
					result.put("initial", new JSONObject(attribute.getDescription().getJSON()));
					snaResult.setAccessMethodObjectResult(result);
				}
				return null;
			}
		};
	}

	final private AccessMethodExecutor unsubscribeExecutor(final ResourceImpl resource) {
		return new AccessMethodExecutor() {
			@Override
			public Void execute(AccessMethodResponseBuilder result) throws Exception {
				String attributeName = (String) result.getParameter(0);
				Attribute attribute = (Attribute) resource.getAttribute(attributeName);
				if (attribute == null) {
					throw new InvalidAttributeException(
							new StringBuilder().append("unknown attribute :").append(attributeName).toString());
				}
				resource.unlisten((String) result.getParameter(1));
				result.setAccessMethodObjectResult(new JSONObject().put("message", "unsubscription done"));
				return null;
			}
		};
	}
}

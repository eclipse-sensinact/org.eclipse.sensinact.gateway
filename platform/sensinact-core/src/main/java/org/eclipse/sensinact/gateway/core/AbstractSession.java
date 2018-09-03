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

import java.util.Enumeration;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstract {@link Session} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSession implements Session {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	protected final String identifier;

	protected <N extends Nameable, E extends ElementsProxy<N>> String joinElementNames(E elements) {
		int index = 0;
		StringBuilder builder = new StringBuilder();
		Enumeration<N> enumeration = elements.elements();
		while (enumeration.hasMoreElements()) {
			N nameable = enumeration.nextElement();
			if (index > 0) {
				builder.append(",");
			}
			builder.append("\"");
			builder.append(nameable.getName());
			builder.append("\"");
			index++;
		}
		return builder.toString();
	}

	protected <T, R extends AccessMethodResponse<T>> R tatooRequestId(String requestId, R response) {
		response.put(AccessMethod.REQUEST_ID_KEY, requestId, requestId == null);
		return response;
	}

	protected <A extends AccessMethodResponse<JSONObject>> A responseFromJSONObject(Mediator mediator, String uri,
			String method, JSONObject object) throws Exception {
		A response = null;
		if (object == null) {
			response = AccessMethodResponse.<JSONObject, A>error(mediator, uri, AccessMethod.Type.valueOf(method),
					SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Not found", null);

		} else {
			object.remove("type");
			object.remove("uri");
			Integer statusCode = (Integer) object.remove("statusCode");

			Class<A> clazz = null;
			switch (method) {
			case "ACT":
				clazz = (Class<A>) ActResponse.class;
				break;
			case "GET":
				clazz = (Class<A>) GetResponse.class;
				break;
			case "SET":
				clazz = (Class<A>) SetResponse.class;
				break;
			case "SUBSCRIBE":
				clazz = (Class<A>) SubscribeResponse.class;
				break;
			case "UNSUBSCRIBE":
				clazz = (Class<A>) UnsubscribeResponse.class;
				break;
			default:
				break;
			}
			if (clazz != null) {
				response = clazz.getConstructor(new Class<?>[] { Mediator.class, String.class, 
					Status.class, int.class }).newInstance(mediator, uri, statusCode.intValue() == 200 
					? Status.SUCCESS : Status.ERROR, statusCode.intValue());

				response.setResponse((JSONObject) object.remove("response"));
				response.setErrors((JSONArray) object.remove("errors"));

				String[] names = JSONObject.getNames(object);
				int index = 0;
				int length = names == null ? 0 : names.length;
				for (; index < length; index++) {
					String name = names[index];
					response.put(name, object.get(name));
				}
			}
		}
		return response;
	}

	protected DescribeResponse<JSONObject> describeFromJSONObject(Mediator mediator,
			DescribeResponseBuilder<JSONObject> builder, DescribeType describeType, JSONObject object) {
		DescribeResponse<JSONObject> response = null;
		if (object == null) {
			String element = describeType.name().toLowerCase();
			String first = element.substring(0, 1).toUpperCase();
			String suite = element.substring(1);

			response = AccessMethodResponse.<JSONObject, DescribeResponse<JSONObject>>error(mediator, builder.getPath(),
					describeType, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
					new StringBuilder().append(first).append(suite).append(" not found").toString(), null);

		} else {
			object.remove("type");
			object.remove("uri");

			builder.setAccessMethodObjectResult((JSONObject) object.remove("response"));

			response = builder
					.createAccessMethodResponse(object.optInt("statusCode") == 200 ? Status.SUCCESS : Status.ERROR);

			response.setErrors((JSONArray) object.remove("errors"));

			String[] names = JSONObject.getNames(object);
			int index = 0;
			int length = names == null ? 0 : names.length;
			for (; index < length; index++) {
				String name = names[index];
				response.put(name, object.get(name));
			}
		}
		return response;
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 *            the String identifier of the Session to be instantiated
	 */
	public AbstractSession(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * return String identifier of this Session
	 * 
	 * @return this Session's identifier
	 */
	public String getSessionId() {
		return this.identifier;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Session#getServiceProviders()
	 */
	@Override
	public Set<ServiceProvider> serviceProviders() {
		return this.serviceProviders(null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# getService(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service service(String serviceProviderName, String serviceName) {
		Service service = null;
		ServiceProvider provider = this.serviceProvider(serviceProviderName);
		if (provider != null) {
			service = provider.getService(serviceName);
		}
		return service;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getResource(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Resource resource(String serviceProviderName, String serviceName, String resourceName) {
		Resource resource = null;
		Service service = null;
		if ((service = this.service(serviceProviderName, serviceName)) != null) {
			resource = service.getResource(resourceName);
		}
		return resource;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#registerSessionAgent(org.eclipse.sensinact.gateway.core.message.MidAgentCallback, 
	 * org.eclipse.sensinact.gateway.core.message.SnaFilter)
	 */
	@Override
	public SubscribeResponse registerSessionAgent(MidAgentCallback callback, SnaFilter filter) {
		return registerSessionAgent(null, callback, filter);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      unregisterSessionAgent(java.lang.String)
	 */
	@Override
	public UnsubscribeResponse unregisterSessionAgent(String agentId) {
		return unregisterSessionAgent(null, agentId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# get(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public GetResponse get(String serviceProviderId, String serviceId, String resourceId, String attributeId) {
		return get(null, serviceProviderId, serviceId, resourceId, attributeId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# set(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public SetResponse set(final String serviceProviderId, final String serviceId, final String resourceId,
			final String attributeId, final Object parameter) {
		return set(null, serviceProviderId, serviceId, resourceId, attributeId, parameter);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# act(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.Object[])
	 */
	public ActResponse act(String serviceProviderId, String serviceId, String resourceId, 
			Object[] parameters) {
		return act(null, serviceProviderId, serviceId, resourceId, parameters);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Session# subscribe(java.lang.String,
	 *      java.lang.String, java.lang.String,org.eclipse.sensinact.gateway.core.message.Recipient,
	 *      org.json.JSONArray)
	 */
	@Override
	public SubscribeResponse subscribe(String serviceProviderId, String serviceId, 
		    String resourceId, Recipient recipient, JSONArray conditions) {
		return subscribe(null, serviceProviderId, serviceId, resourceId, recipient, conditions);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Session#subscribe(java.lang.String, java.lang.String, 
	 * java.lang.String, org.eclipse.sensinact.gateway.core.message.Recipient, org.json.JSONArray, 
	 * java.lang.String)
	 */
	@Override
	public SubscribeResponse subscribe(String serviceProviderId, String serviceId, 
		    String resourceId, Recipient recipient, JSONArray conditions, String policy) {
		return subscribe(null, serviceProviderId, serviceId, resourceId, recipient, conditions, policy);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Session#subscribe(java.lang.String, java.lang.String, 
	 * java.lang.String, java.lang.String, org.eclipse.sensinact.gateway.core.message.Recipient, 
	 * org.json.JSONArray)
	 */
	@Override
	public SubscribeResponse subscribe(String requestId, String serviceProviderId, String serviceId, 
		    String resourceId, Recipient recipient, JSONArray conditions) {
		return subscribe(requestId, serviceProviderId, serviceId, resourceId, recipient, 
				conditions, String.valueOf(ErrorHandler.Policy.DEFAULT_POLICY));
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      unsubscribe(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public UnsubscribeResponse unsubscribe(String serviceProviderId, String serviceId, 
			final String resourceId, String subscriptionId) {
		return unsubscribe(null, serviceProviderId, serviceId, resourceId, subscriptionId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# getAll(java.lang.String,
	 *      org.eclipse.sensinact.gateway.core.FilteringDefinition)
	 */
	@Override
	public DescribeResponse<String> getAll() {
		return getAll(null, null, null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# getAll(java.lang.String,
	 *      org.eclipse.sensinact.gateway.core.FilteringDefinition)
	 */
	@Override
	public DescribeResponse<String> getAll(FilteringCollection filterCollection) {
		return getAll(null, null, filterCollection);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# getAll(java.lang.String,
	 *      org.eclipse.sensinact.gateway.core.FilteringDefinition)
	 */
	@Override
	public DescribeResponse<String> getAll(String filter, FilteringCollection filterCollection) {
		return getAll(null, filter, filterCollection);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#getProviders()
	 */
	@Override
	public DescribeResponse<String> getProviders() {
		return getProviders(null, null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getProviders(org.eclipse.sensinact.gateway.core.FilteringCollection)
	 */
	@Override
	public DescribeResponse<String> getProviders(FilteringCollection filterCollection) {
		return getProviders(null, filterCollection);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getProvider(java.lang.String)
	 */
	@Override
	public DescribeResponse<JSONObject> getProvider(String serviceProviderId) {
		return getProvider(null, serviceProviderId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getServices(java.lang.String)
	 */
	@Override
	public DescribeResponse<String> getServices(String serviceProviderId) {
		return getServices(null, serviceProviderId, null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getServices(java.lang.String,
	 *      org.eclipse.sensinact.gateway.core.FilteringCollection)
	 */
	@Override
	public DescribeResponse<String> getServices(final String serviceProviderId, FilteringCollection filterCollection) {
		return getServices(null, serviceProviderId, filterCollection);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session# getService(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public DescribeResponse<JSONObject> getService(final String serviceProviderId, final String serviceId) {
		return getService(null, serviceProviderId, serviceId);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getResources(java.lang.String, java.lang.String)
	 */
	@Override
	public DescribeResponse<String> getResources(final String serviceProviderId, final String serviceId) {
		return getResources(null, serviceProviderId, serviceId, null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getResources(java.lang.String, java.lang.String,
	 *      org.eclipse.sensinact.gateway.core.FilteringDefinition)
	 */
	@Override
	public DescribeResponse<String> getResources(final String serviceProviderId, final String serviceId,
			FilteringCollection filterCollection) {
		return getResources(null, serviceProviderId, serviceId, filterCollection);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Session#
	 *      getResource(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public DescribeResponse<JSONObject> getResource(final String serviceProviderId, final String serviceId,
			final String resourceId) {
		return getResource(null, serviceProviderId, serviceId, resourceId);
	}
}

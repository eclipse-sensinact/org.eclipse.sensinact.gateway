/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import java.util.Enumeration;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.ActResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Abstract {@link Session} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSession implements Session {

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
		if(response == null){
		    return null;
		}
		response.put(AccessMethod.REQUEST_ID_KEY, requestId, requestId == null);
		return response;
	}

	@SuppressWarnings("unchecked")
	protected <A extends AccessMethodResponse<JsonObject>> A responseFromJSONObject(Mediator mediator, String uri,
			String method, JsonObject object) throws Exception {
		if (object == null) 
			return AccessMethodResponse.<JsonObject, A>error(mediator, uri, AccessMethod.Type.valueOf(method),
				SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Not found", null);
		else {			
			switch (method) {
			case "ACT":
				return (A) this.<ActResponse>responseFromJSONObject(ActResponse.class, mediator, uri, method, object);
			case "GET":
				return (A) this.<GetResponse>responseFromJSONObject(GetResponse.class, mediator, uri, method, object);
			case "SET":
				return (A) this.<SetResponse>responseFromJSONObject(SetResponse.class, mediator, uri, method, object);
			case "SUBSCRIBE":
				return (A) this.<SubscribeResponse>responseFromJSONObject(SubscribeResponse.class, mediator, uri, 
						method, object);
			case "UNSUBSCRIBE":
				return (A) this.<UnsubscribeResponse>responseFromJSONObject(UnsubscribeResponse.class, mediator, uri, 
						method, object);
			default:
			    break;
			}
		}
		return (A) null;
	}

	protected <A extends AccessMethodResponse<JsonObject>> A responseFromJSONObject(Class<A> responseType, Mediator mediator, 
		String uri, String method, JsonObject object) throws Exception {
		A response = null;
		if (object == null) 
			response = AccessMethodResponse.<JsonObject, A>error(mediator, uri, AccessMethod.Type.valueOf(method),
				SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Not found", null);
		else {
			object.remove("type");
			object.remove("uri");
			Integer statusCode = object.getInt("statusCode");
			if (responseType != null) {
				response = responseType.getConstructor(new Class<?>[] { String.class, 
					Status.class, int.class }).newInstance(uri, statusCode.intValue() == 200 
					? Status.SUCCESS : Status.ERROR, statusCode.intValue());

				response.setResponse(object.getJsonObject("response"));
				response.setErrors(object.getJsonArray("errors"));

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

	protected DescribeResponse<JsonObject> describeFromJSONObject(Mediator mediator, DescribeResponseBuilder<JsonObject> builder, 
		DescribeType describeType, JsonObject object) {
		DescribeResponse<JsonObject> response = null;
		if (object == null) {
			String element = describeType.name().toLowerCase();
			String first = element.substring(0, 1).toUpperCase();
			String suite = element.substring(1);

			response = AccessMethodResponse.<JsonObject, DescribeResponse<JsonObject>>error(mediator, builder.getPath(),
					describeType, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
					new StringBuilder().append(first).append(suite).append(" not found").toString(), null);

		} else {
			object.remove("type");
			object.remove("uri");

			builder.setAccessMethodObjectResult(object.getJsonObject("response"));

			response = builder
					.createAccessMethodResponse(object.getInt("statusCode", -1) == 200 ? Status.SUCCESS : Status.ERROR);

			response.setErrors(object.getJsonArray("errors"));

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

	@Override
	public Set<ServiceProvider> serviceProviders() {
		return this.serviceProviders(null);
	}

	@Override
	public Service service(String serviceProviderName, String serviceName) {
		Service service = null;
		ServiceProvider provider = this.serviceProvider(serviceProviderName);
		if (provider != null) {
			service = provider.getService(serviceName);
		}
		return service;
	}

	@Override
	public Resource resource(String serviceProviderName, String serviceName, String resourceName) {
		Resource resource = null;
		Service service = null;
		if ((service = this.service(serviceProviderName, serviceName)) != null) {
			resource = service.getResource(resourceName);
		}
		return resource;
	}

	@Override
	public SubscribeResponse registerSessionIntent(Executable<Boolean,Void> callback, String... resourcePath) {
		return registerSessionIntent(null, callback, resourcePath);
	}

	@Override
	public UnsubscribeResponse unregisterSessionIntent(String intentId) {
		return unregisterSessionIntent(null, intentId);
	}
	
	@Override
	public SubscribeResponse registerSessionAgent(MidAgentCallback callback, SnaFilter filter) {
		return registerSessionAgent(null, callback, filter);
	}

	@Override
	public UnsubscribeResponse unregisterSessionAgent(String agentId) {
		return unregisterSessionAgent(null, agentId);
	}

	@Override
	public GetResponse get(String serviceProviderId, String serviceId, String resourceId, String attributeId, Object...args) {
		return get(null, serviceProviderId, serviceId, resourceId, attributeId, args);
	}

	@Override
	public SetResponse set(final String serviceProviderId, final String serviceId, final String resourceId,
			final String attributeId, final Object parameter, Object... args) {
		return set(null, serviceProviderId, serviceId, resourceId, attributeId, parameter, args);
	}

	@Override
	public ActResponse act(String serviceProviderId, String serviceId, String resourceId, Object[] parameters) {
		return act(null, serviceProviderId, serviceId, resourceId, parameters);
	}

	@Override
	public SubscribeResponse subscribe(String serviceProviderId, String serviceId, 
		    String resourceId, Recipient recipient, JsonArray conditions, Object...args) {
		return subscribe(null, serviceProviderId, serviceId, resourceId, recipient, conditions, args);
	}

	@Override
	public SubscribeResponse subscribe(String serviceProviderId, String serviceId, 
		    String resourceId, Recipient recipient, JsonArray conditions, String policy, Object...args) {
		return subscribe(null, serviceProviderId, serviceId, resourceId, recipient, conditions, policy, args);
	}

	@Override
	public SubscribeResponse subscribe(String requestId, String serviceProviderId, String serviceId, 
		    String resourceId, Recipient recipient, JsonArray conditions, Object...args) {
		return subscribe(requestId, serviceProviderId, serviceId, resourceId, recipient, 
				conditions, String.valueOf(ErrorHandler.Policy.DEFAULT_POLICY), args);
	}

	@Override
	public UnsubscribeResponse unsubscribe(String serviceProviderId, String serviceId, 
			final String resourceId, String subscriptionId, Object...args) {
		return unsubscribe(null, serviceProviderId, serviceId, resourceId, subscriptionId, args);
	}

	@Override
	public DescribeResponse<String> getAll() {
		return getAll(null, null, null);
	}

	@Override
	public DescribeResponse<String> getAll(FilteringCollection filterCollection) {
		return getAll(null, null, filterCollection);
	}

	@Override
	public DescribeResponse<String> getAll(String filter, FilteringCollection filterCollection) {
		return getAll(null, filter, filterCollection);
	}

	@Override
	public DescribeResponse<String> getProviders() {
		return getProviders(null, null);
	}

	@Override
	public DescribeResponse<String> getProviders(FilteringCollection filterCollection) {
		return getProviders(null, filterCollection);
	}

	@Override
	public DescribeResponse<JsonObject> getProvider(String serviceProviderId) {
		return getProvider(null, serviceProviderId);
	}

	@Override
	public DescribeResponse<String> getServices(String serviceProviderId) {
		return getServices(null, serviceProviderId, null);
	}

	@Override
	public DescribeResponse<String> getServices(final String serviceProviderId, FilteringCollection filterCollection) {
		return getServices(null, serviceProviderId, filterCollection);
	}

	@Override
	public DescribeResponse<JsonObject> getService(final String serviceProviderId, final String serviceId) {
		return getService(null, serviceProviderId, serviceId);
	}

	@Override
	public DescribeResponse<String> getResources(final String serviceProviderId, final String serviceId) {
		return getResources(null, serviceProviderId, serviceId, null);
	}

	@Override
	public DescribeResponse<String> getResources(final String serviceProviderId, final String serviceId,
			FilteringCollection filterCollection) {
		return getResources(null, serviceProviderId, serviceId, filterCollection);
	}

	@Override
	public DescribeResponse<JsonObject> getResource(final String serviceProviderId, final String serviceId,
			final String resourceId) {
		return getResource(null, serviceProviderId, serviceId, resourceId);
	}
}
